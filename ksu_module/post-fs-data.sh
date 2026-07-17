#!/system/bin/sh
# LiteLauncher Helper — Kernel VM tuning (runs early, BLOCKING — max 10s)

MODDIR=${0%/*}

# === VM Parameters ===

# Swappiness: 80-100 optimal for zRAM + flash swap
echo 100 > /proc/sys/vm/swappiness

# Cache pressure: higher = reclaim dentry/inode more aggressively
echo 150 > /proc/sys/vm/vfs_cache_pressure

# Dirty page ratios — reduce IO spikes during MC chunk saves
echo 10 > /proc/sys/vm/dirty_background_ratio
echo 30 > /proc/sys/vm/dirty_ratio

# Transparent Hugepages — MC JVM works better with THP disabled
if [ -f /sys/kernel/mm/transparent_hugepage/enabled ]; then
    echo madvise > /sys/kernel/mm/transparent_hugepage/enabled 2>/dev/null || true
fi
if [ -f /sys/kernel/mm/transparent_hugepage/defrag ]; then
    echo madvise > /sys/kernel/mm/transparent_hugepage/defrag 2>/dev/null || true
fi

# Overcommit — JVM mmap needs this
echo 1 > /proc/sys/vm/overcommit_memory 2>/dev/null || true
echo 100 > /proc/sys/vm/overcommit_ratio 2>/dev/null || true

# Watermark scale — allow larger kswapd bursts
echo 100 > /proc/sys/vm/watermark_scale_factor 2>/dev/null || true

# === zRAM Extension ===

# Resize zRAM to 50% of total RAM
TOTAL_RAM_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
ZRAM_SIZE=$(( TOTAL_RAM_KB * 50 / 100 ))K

if [ -e /sys/block/zram0/disksize ]; then
    # Reset zRAM (requires swapoff first if active)
    swapoff /dev/block/zram0 2>/dev/null || true
    echo 1 > /sys/block/zram0/reset 2>/dev/null || true
    
    # Set compression algorithm (lz4 fastest, zstd best ratio)
    if echo lz4 > /sys/block/zram0/comp_algorithm 2>/dev/null; then
        :
    elif echo lzo > /sys/block/zram0/comp_algorithm 2>/dev/null; then
        :
    fi
    
    # Set new size
    echo "$ZRAM_SIZE" > /sys/block/zram0/disksize 2>/dev/null || true
    
    # Re-enable
    mkswap /dev/block/zram0 2>/dev/null || true
    swapon /dev/block/zram0 -p 100 2>/dev/null || true
fi

# === Scene8G Swap Detection ===

# Look for existing swap file (Scene8G typical location)
SCENE_SWAP=""
for candidate in /data/swap/swapfile /data/vendor/swap/swapfile; do
    if [ -f "$candidate" ]; then
        SCENE_SWAP="$candidate"
        break
    fi
done

if [ -n "$SCENE_SWAP" ]; then
    # Activate with lower priority than zRAM (zRAM=100, flash=10)
    swapon "$SCENE_SWAP" -p 10 2>/dev/null || true
fi

echo "[litelauncher] post-fs-data complete: zRAM=${ZRAM_SIZE}, Scene8G=${SCENE_SWAP:-none}" > /dev/kmsg
