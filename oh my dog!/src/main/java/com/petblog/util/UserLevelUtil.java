package com.petblog.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户等级工具类
 * 管理用户等级计算和等级信息
 */
public class UserLevelUtil {
    
    // 等级配置：等级 -> {名称, 图标, 所需XP}
    private static final Map<Integer, LevelInfo> LEVEL_CONFIG = new HashMap<>();
    
    static {
        LEVEL_CONFIG.put(1, new LevelInfo("小奶汪", "🐶", 0));
        LEVEL_CONFIG.put(2, new LevelInfo("绒毛幼崽", "🐕", 20));
        LEVEL_CONFIG.put(3, new LevelInfo("舔爪萌新", "🐾", 60));
        LEVEL_CONFIG.put(4, new LevelInfo("拆家预备员", "🦴", 120));
        LEVEL_CONFIG.put(5, new LevelInfo("遛弯爱好者", "🏃", 200));
        LEVEL_CONFIG.put(6, new LevelInfo("活力少年", "⚡", 300));
        LEVEL_CONFIG.put(7, new LevelInfo("追风者", "🌪️", 450));
        LEVEL_CONFIG.put(8, new LevelInfo("狗界领袖", "👑", 650));
        LEVEL_CONFIG.put(9, new LevelInfo("镇宅神兽", "🦁", 900));
        LEVEL_CONFIG.put(10, new LevelInfo("汪星传奇", "⭐", 1200));
    }
    
    /**
     * 根据XP计算用户等级
     * @param xp 用户经验值
     * @return 等级 (1-10)
     */
    public static int calculateLevel(int xp) {
        int level = 1;
        for (int i = 10; i >= 1; i--) {
            if (xp >= LEVEL_CONFIG.get(i).requiredXp) {
                level = i;
                break;
            }
        }
        return level;
    }
    
    /**
     * 获取等级信息
     * @param level 等级 (1-10)
     * @return 等级信息对象
     */
    public static LevelInfo getLevelInfo(int level) {
        if (level < 1) level = 1;
        if (level > 10) level = 10;
        return LEVEL_CONFIG.get(level);
    }
    
    /**
     * 获取等级名称
     * @param level 等级
     * @return 等级名称
     */
    public static String getLevelName(int level) {
        return getLevelInfo(level).name;
    }
    
    /**
     * 获取等级图标
     * @param level 等级
     * @return 等级图标
     */
    public static String getLevelIcon(int level) {
        return getLevelInfo(level).icon;
    }
    
    /**
     * 获取下一级所需XP
     * @param currentLevel 当前等级
     * @return 下一级所需XP，如果已满级返回-1
     */
    public static int getNextLevelXp(int currentLevel) {
        if (currentLevel >= 10) {
            return -1; // 已满级
        }
        return LEVEL_CONFIG.get(currentLevel + 1).requiredXp;
    }
    
    /**
     * 等级信息内部类
     */
    public static class LevelInfo {
        public final String name;
        public final String icon;
        public final int requiredXp;
        
        public LevelInfo(String name, String icon, int requiredXp) {
            this.name = name;
            this.icon = icon;
            this.requiredXp = requiredXp;
        }
    }
}

