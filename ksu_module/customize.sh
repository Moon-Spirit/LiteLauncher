#!/system/bin/sh
# LiteLauncher Helper — KSU Module Installer
SKIPUNZIP=0

ui_print "- LiteLauncher Helper v1.0.0"
ui_print "- Kernel-level MC Runtime protection"
ui_print "- OOM adj -1000 | Nice -20 | GPU perf | zRAM 50%"

# Detect SoC
HW=$(getprop ro.hardware 2>/dev/null)
case "$HW" in
    qcom|QRD*|SM*|msm*|sdm*|sm*)
        ui_print "- Detected: Qualcomm SoC"
        ;;
    mt*|MT*)
        ui_print "- Detected: MediaTek SoC"
        ;;
    *)
        ui_print "- SoC: unknown (generic tuning)"
        ;;
esac

# RAM info
TOTAL_RAM_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
TOTAL_RAM_MB=$(( TOTAL_RAM_KB / 1024 ))
ui_print "- RAM: ${TOTAL_RAM_MB}MB"

# Set permissions
set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm_recursive "$MODPATH/system/bin" 0 0 0755 0755

# Initialize KSU module config defaults
if command -v ksud >/dev/null 2>&1; then
    ksud module config set oom_score_adj -1000
    ksud module config set nice_val -20
    ksud module config set cpu_mask ff
    ksud module config set sched_priority 99
    ksud module config set override.description "MC Runtime: OOM -1000 | Nice -20"
fi

ui_print "- Installation complete"
ui_print "- Control via KSU Manager → LiteLauncher Helper (WebUI)"
ui_print "- Or: echo PID > /data/local/tmp/litelauncher.pid"
