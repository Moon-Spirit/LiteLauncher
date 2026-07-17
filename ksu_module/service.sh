#!/system/bin/sh
# LiteLauncher Helper — MC Runtime Process Protection Daemon
# Runs in late_start service mode (NON-BLOCKING)

MODDIR=${0%/*}
LOG="$MODDIR/protect.log"
PID_FILE="/data/local/tmp/litelauncher.pid"

# Wait for boot completion
while [ "$(getprop sys.boot_completed)" != "1" ]; do
    sleep 2
done

# Read config from KSU module config system (persistent across reboots)
OOM_ADJ=$(ksud module config get oom_score_adj 2>/dev/null)
[ -z "$OOM_ADJ" ] && OOM_ADJ=-1000

NICE_VAL=$(ksud module config get nice_val 2>/dev/null)
[ -z "$NICE_VAL" ] && NICE_VAL=-20

CPU_MASK=$(ksud module config get cpu_mask 2>/dev/null)
[ -z "$CPU_MASK" ] && CPU_MASK=ff

SCHED_PRIO=$(ksud module config get sched_priority 2>/dev/null)
[ -z "$SCHED_PRIO" ] && SCHED_PRIO=99

# Update KSU Manager description
ksud module config set override.description "MC Runtime: OOM $OOM_ADJ | Nice $NICE_VAL | Mask 0x$CPU_MASK"

echo "[$(date)] LiteLauncher Helper started" > "$LOG"
echo "[$(date)] Config: OOM=$OOM_ADJ NICE=$NICE_VAL CPU=0x$CPU_MASK SCHED=$SCHED_PRIO" >> "$LOG"

# === SoC Detection ===
detect_soc() {
    local hw=$(getprop ro.hardware 2>/dev/null)
    case "$hw" in
        qcom|QRD*|SM*|msm*|sdm*|sm*)
            echo "qcom"
            ;;
        mt*|MT*)
            echo "mtk"
            ;;
        *)
            # Fallback: check cpuinfo
            if grep -qi qcom /proc/cpuinfo 2>/dev/null; then
                echo "qcom"
            elif grep -qi "mediatek\|MT" /proc/cpuinfo 2>/dev/null; then
                echo "mtk"
            else
                echo "unknown"
            fi
            ;;
    esac
}

SOC=$(detect_soc)

# === GPU Governor: Performance ===
setup_gpu() {
    case "$SOC" in
        qcom)
            local gpu_path=""
            for p in /sys/class/kgsl/kgsl-3d0 /sys/devices/platform/soc/*.qcom,kgsl-3d0/kgsl/kgsl-3d0; do
                if [ -d "$p" ]; then
                    gpu_path="$p"
                    break
                fi
            done
            if [ -n "$gpu_path" ] && [ -f "$gpu_path/devfreq/governor" ]; then
                echo performance > "$gpu_path/devfreq/governor" 2>/dev/null || true
                echo "[$(date)] GPU: qcom performance governor set" >> "$LOG"
            fi
            if [ -f "$gpu_path/max_gpuclk" ]; then
                local max_freq=$(cat "$gpu_path/max_gpuclk" 2>/dev/null)
                echo "$max_freq" > "$gpu_path/gpuclk" 2>/dev/null || true
            fi
            ;;
        mtk)
            if [ -f /proc/gpufreq/gpufreq_opp_freq ]; then
                local max_freq=$(head -1 /proc/gpufreq/gpufreq_opp_freq 2>/dev/null)
                echo "$max_freq" > /proc/gpufreq/gpufreq_opp_freq 2>/dev/null || true
                echo "[$(date)] GPU: mtk max frequency set" >> "$LOG"
            fi
            ;;
    esac
}

setup_gpu

# === Main Protection Loop ===
while true; do
    PID=""
    
    # Check PID file (written by LiteLauncher app before game launch)
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE" 2>/dev/null)
    fi
    
    # Validate PID exists and belongs to MC Runtime process
    if [ -n "$PID" ] && [ "$PID" -gt 0 ] 2>/dev/null; then
        if [ -f "/proc/$PID/comm" ]; then
            PROC_NAME=$(cat "/proc/$PID/cmdline" 2>/dev/null | tr '\0' ' ' | head -c 80)
            
            if echo "$PROC_NAME" | grep -q "mc_runtime\|litelauncher"; then
                # === 1. OOM Killer Protection ===
                echo "$OOM_ADJ" > "/proc/$PID/oom_score_adj" 2>/dev/null || true
                
                # === 2. CPU Affinity — Bind to Big Cores ===
                taskset -p -a "0x$CPU_MASK" "$PID" 2>/dev/null || true
                
                # === 3. Priority ===
                renice -n "$NICE_VAL" -p "$PID" 2>/dev/null || true
                
                # === 4. SCHED_FIFO (Real-time) Scheduling ===
                chrt -f -p "$SCHED_PRIO" "$PID" 2>/dev/null || chrt -r -p $((SCHED_PRIO/2)) "$PID" 2>/dev/null || true
                
                # === 5. IO Priority ===
                ionice -c 1 -n 0 -p "$PID" 2>/dev/null || true
                
                # Log every 5 minutes
                if [ $(( $(date +%s) % 300 )) -lt 10 ]; then
                    OOM_CUR=$(cat "/proc/$PID/oom_score_adj" 2>/dev/null || echo "err")
                    RSS_KB=$(grep VmRSS "/proc/$PID/status" 2>/dev/null | awk '{print $2}')
                    echo "[$(date)] PID=$PID OOM=$OOM_CUR RSS=${RSS_KB}KB" >> "$LOG"
                fi
            fi
        else
            # Process no longer exists — remove stale PID file
            rm -f "$PID_FILE"
        fi
    fi
    
    # Trim log if too large
    if [ -f "$LOG" ] && [ "$(wc -c < "$LOG" 2>/dev/null)" -gt 65536 ]; then
        tail -n 100 "$LOG" > "${LOG}.tmp" && mv "${LOG}.tmp" "$LOG"
    fi
    
    sleep 10
done
