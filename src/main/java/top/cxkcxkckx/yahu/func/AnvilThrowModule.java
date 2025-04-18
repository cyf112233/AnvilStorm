package top.cxkcxkckx.yahu.func;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import top.cxkcxkckx.yahu.yahu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AnvilThrowModule extends AbstractModule implements Listener {
    private double maxChargeTime; // 最大蓄力时间（秒）
    private double maxVelocity; // 最大速度
    private boolean enabled; // 是否启用功能
    private int logLevel; // 0=关闭, 1=最小, 2=最大
    private String damageType; // 伤害类型：auto=自动计算，number=固定值
    private double fixedDamage; // 固定伤害值
    private double maxDamage; // 最大伤害值
    private double damageRadius; // 伤害范围
    private String chargeBarType; // 蓄力条类型
    private String bossBarColor; // Boss血条颜色
    private final yahu plugin;
    private final Map<UUID, Long> chargingPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> anvilSpawnTimes = new ConcurrentHashMap<>(); // 记录铁砧生成时间
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>(); // 记录玩家的Boss血条
    private final Random random = new Random();

    public AnvilThrowModule(yahu plugin) {
        super(plugin);
        this.plugin = plugin;
        // 从配置文件加载设置
        loadConfig();
    }
    
    /**
     * 从配置文件加载设置
     */
    private void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("anvil-throw.enabled", true);
        this.maxChargeTime = plugin.getConfig().getDouble("anvil-throw.max-charge-time", 2.0);
        this.maxVelocity = plugin.getConfig().getDouble("anvil-throw.max-velocity", 2.0);
        this.logLevel = plugin.getConfig().getInt("anvil-throw.log-level", 0); // 默认关闭日志
        this.damageType = plugin.getConfig().getString("anvil-throw.damage.type", "auto");
        this.fixedDamage = plugin.getConfig().getDouble("anvil-throw.damage.fixed-damage", 20.0);
        this.maxDamage = plugin.getConfig().getDouble("anvil-throw.damage.max-damage", 40.0);
        this.damageRadius = plugin.getConfig().getDouble("anvil-throw.damage.radius", 1.5);
        this.chargeBarType = plugin.getConfig().getString("anvil-throw.charge-bar.type", "exp");
        this.bossBarColor = plugin.getConfig().getString("anvil-throw.charge-bar.color", "red");
        plugin.getLogger().info("铁砧投掷功能配置已加载: enabled=" + enabled + 
            ", maxChargeTime=" + maxChargeTime + 
            ", maxVelocity=" + maxVelocity + 
            ", logLevel=" + logLevel +
            ", damageType=" + damageType +
            ", fixedDamage=" + fixedDamage +
            ", maxDamage=" + maxDamage +
            ", damageRadius=" + damageRadius +
            ", chargeBarType=" + chargeBarType +
            ", bossBarColor=" + bossBarColor);
    }

    private void logInfo(String message) {
        if (logLevel >= 1) {
            plugin.getLogger().info(message);
        }
    }

    private void logDebug(String message) {
        if (logLevel >= 2) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * 更新蓄力条显示
     * @param player 玩家
     * @param progress 进度（0-1）
     */
    private void updateChargeBar(Player player, double progress) {
        // 根据配置的类型更新对应的蓄力条
        switch (chargeBarType) {
            case "exp":
                updateExpBar(player, progress);
                break;
            case "bossbar":
                updateBossBar(player, progress);
                break;
            case "actionbar":
                updateActionBar(player, progress);
                break;
        }
    }

    /**
     * 更新经验条显示
     */
    private void updateExpBar(Player player, double progress) {
        player.setExp((float) progress);
        player.setLevel((int) (progress * 100));
    }

    /**
     * 更新Boss血条显示
     */
    private void updateBossBar(Player player, double progress) {
        if (chargeBarType.equals("bossbar")) {
            BossBar bossBar = bossBars.computeIfAbsent(player.getUniqueId(), uuid -> {
                BarColor color = BarColor.valueOf(bossBarColor.toUpperCase());
                BossBar bar = plugin.getServer().createBossBar(
                    "铁砧蓄力",
                    color,
                    BarStyle.SOLID
                );
                bar.addPlayer(player);
                return bar;
            });
            bossBar.setProgress(progress);
        }
    }

    /**
     * 更新动作栏显示
     */
    private void updateActionBar(Player player, double progress) {
        if (chargeBarType.equals("actionbar")) {
            int percentage = (int) (progress * 100);
            int filledBars = (int) (progress * 20);
            StringBuilder bar = new StringBuilder("§e铁砧蓄力: §f[");
            
            String colorCode = getColorCode();
            
            for (int i = 0; i < 20; i++) {
                if (i < filledBars) {
                    bar.append(colorCode).append("█");
                } else {
                    bar.append("§7░");
                }
            }
            bar.append("§f] §e").append(percentage).append("%");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar.toString()));
        }
    }

    /**
     * 获取颜色代码
     */
    private String getColorCode() {
        switch (bossBarColor.toLowerCase()) {
            case "red": return "§c";
            case "green": return "§a";
            case "blue": return "§9";
            case "yellow": return "§e";
            case "purple": return "§5";
            case "white": return "§f";
            default: return "§a";
        }
    }

    /**
     * 清除蓄力条显示
     * @param player 玩家
     */
    private void clearChargeBar(Player player) {
        // 根据配置的类型清除对应的蓄力条
        switch (chargeBarType) {
            case "exp":
                clearExpBar(player);
                break;
            case "bossbar":
                clearBossBar(player);
                break;
            case "actionbar":
                clearActionBar(player);
                break;
        }
    }

    /**
     * 清除经验条显示
     */
    private void clearExpBar(Player player) {
        player.setExp(0);
        player.setLevel(0);
    }

    /**
     * 清除Boss血条显示
     */
    private void clearBossBar(Player player) {
        if (chargeBarType.equals("bossbar")) {
            BossBar bossBar = bossBars.remove(player.getUniqueId());
            if (bossBar != null) {
                bossBar.removePlayer(player);
            }
        }
    }

    /**
     * 清除动作栏显示
     */
    private void clearActionBar(Player player) {
        if (chargeBarType.equals("actionbar")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
    }

    public void onEnable() {
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            logInfo("铁砧投掷功能已启用");
            
            // 注册定时任务来更新蓄力条和播放音效
            plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                for (Map.Entry<UUID, Long> entry : chargingPlayers.entrySet()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null && player.isSneaking()) {
                        long startTime = entry.getValue();
                        double chargeTime = (System.currentTimeMillis() - startTime) / 1000.0;
                        
                        // 计算蓄力进度（0-1）
                        double progress = Math.min(chargeTime / maxChargeTime, 1.0);
                        
                        // 更新所有蓄力条显示
                        updateChargeBar(player, progress);
                        
                        // 播放蓄力音效
                        if (chargeTime < maxChargeTime) {
                            // 音调从0.5到2.0，随进度增加
                            float pitch = 0.5F + (float) progress * 1.5F;
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, pitch);
                        } else {
                            // 蓄力满时播放最大音效
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
                        }
                    } else {
                        // 玩家停止蓄力，清除所有蓄力条显示
                        chargingPlayers.remove(player.getUniqueId());
                        if (player != null) {
                            clearChargeBar(player);
                        }
                    }
                }
            }, 0L, 2L); // 每2个游戏刻（0.1秒）更新一次
        } else {
            logInfo("铁砧投掷功能已禁用");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        logDebug("玩家潜行状态改变: " + player.getName() + ", 状态: " + event.isSneaking() + ", 物品: " + item.getType());

        // 检查是否是铁砧（包括所有变种）
        Material itemType = item.getType();
        if (itemType != Material.ANVIL && 
            itemType != Material.CHIPPED_ANVIL && 
            itemType != Material.DAMAGED_ANVIL) {
            logDebug("不是铁砧，忽略");
            return;
        }
        
        // 检查功能是否启用
        if (!enabled) {
            logDebug("功能未启用，忽略");
            return;
        }

        if (event.isSneaking()) {
            // 开始蓄力
            logDebug("玩家开始蓄力");
            chargingPlayers.put(player.getUniqueId(), System.currentTimeMillis());
            // 播放开始蓄力的音效
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.5F);
        } else {
            // 结束蓄力并投掷
            Long startTime = chargingPlayers.remove(player.getUniqueId());
            if (startTime != null) {
                logDebug("玩家释放蓄力");
                double chargeTime = (System.currentTimeMillis() - startTime) / 1000.0;
                double velocity = Math.min((chargeTime / maxChargeTime) * maxVelocity, maxVelocity);
                
                if (velocity > 0) {
                    // 创建掉落方块，保持相同的铁砧类型
                    FallingBlock anvil = player.getWorld().spawnFallingBlock(
                        player.getEyeLocation(),
                        itemType.createBlockData()
                    );

                    // 设置初始速度
                    Vector direction = player.getLocation().getDirection();
                    direction.multiply(velocity);
                    direction.setY(direction.getY() + 0.5); // 添加一些向上的力
                    anvil.setVelocity(direction);

                    // 记录铁砧生成时间
                    anvilSpawnTimes.put(anvil.getUniqueId(), System.currentTimeMillis());

                    // 移除玩家手中的铁砧
                    item.setAmount(item.getAmount() - 1);
                    logDebug("铁砧已投掷，速度: " + velocity);
                    
                    // 清除蓄力条
                    clearChargeBar(player);

                    // 播放投掷音效
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0F, 1.0F);
                }
            }
        }
    }

    /**
     * 计算铁砧伤害
     * @param fallingBlock 铁砧实体
     * @return 伤害值
     */
    private double calculateDamage(FallingBlock fallingBlock) {
        if ("number".equals(damageType)) {
            return fixedDamage;
        }
        
        // 自动计算伤害
        Long spawnTime = anvilSpawnTimes.get(fallingBlock.getUniqueId());
        if (spawnTime == null) {
            return fixedDamage; // 如果没有记录生成时间，使用固定伤害
        }
        
        // 计算滞空时间（秒）
        double airTime = (System.currentTimeMillis() - spawnTime) / 1000.0;
        // 根据滞空时间计算伤害，每0.5秒增加2点伤害，最大伤害由配置决定
        double damage = Math.min(airTime * 4, maxDamage);
        logDebug("铁砧滞空时间: " + airTime + "秒，计算伤害: " + damage);
        return damage;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAnvilLand(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;
        
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        Material material = fallingBlock.getBlockData().getMaterial();
        
        // 检查是否是铁砧
        if (material != Material.ANVIL && 
            material != Material.CHIPPED_ANVIL && 
            material != Material.DAMAGED_ANVIL) return;

        // 播放落地音效
        fallingBlock.getWorld().playSound(fallingBlock.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1.0F, 1.0F);

        // 计算伤害
        double damage = calculateDamage(fallingBlock);
        
        // 对周围生物造成伤害
        for (LivingEntity entity : fallingBlock.getWorld().getLivingEntities()) {
            if (entity.getLocation().distance(fallingBlock.getLocation()) <= damageRadius) {
                // 使用铁砧作为伤害来源
                entity.damage(damage, fallingBlock);
                logDebug("铁砧落地砸中 " + entity.getType() + "，造成 " + damage + " 点伤害");
            }
        }

        // 移除铁砧生成时间记录
        anvilSpawnTimes.remove(fallingBlock.getUniqueId());

        // 铁砧损坏机制
        if (random.nextDouble() < 0.12) { // 12% 的几率损坏
            Material nextState = getNextDamagedState(material);
            if (nextState != null) {
                event.setCancelled(true);
                fallingBlock.getWorld().getBlockAt(event.getBlock().getLocation()).setType(nextState);
                // 播放损坏音效
                fallingBlock.getWorld().playSound(fallingBlock.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_DESTROY, 1.0F, 1.0F);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAnvilHitEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof FallingBlock)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        FallingBlock fallingBlock = (FallingBlock) event.getDamager();
        Material material = fallingBlock.getBlockData().getMaterial();
        
        // 检查是否是铁砧
        if (material != Material.ANVIL && 
            material != Material.CHIPPED_ANVIL && 
            material != Material.DAMAGED_ANVIL) return;

        // 计算伤害
        double damage = calculateDamage(fallingBlock);
        event.setDamage(damage);

        // 播放击中音效
        fallingBlock.getWorld().playSound(fallingBlock.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1.0F, 0.5F);

        // 移除铁砧生成时间记录
        anvilSpawnTimes.remove(fallingBlock.getUniqueId());

        // 铁砧损坏机制
        if (random.nextDouble() < 0.12) { // 12% 的几率损坏
            Material nextState = getNextDamagedState(material);
            if (nextState != null) {
                fallingBlock.getWorld().getBlockAt(fallingBlock.getLocation()).setType(nextState);
                // 播放损坏音效
                fallingBlock.getWorld().playSound(fallingBlock.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_DESTROY, 1.0F, 1.0F);
            }
        }

        // 记录伤害信息
        LivingEntity victim = (LivingEntity) event.getEntity();
        logDebug("铁砧砸中 " + victim.getType() + "，造成 " + damage + " 点伤害");
    }

    private Material getNextDamagedState(Material current) {
        switch (current) {
            case ANVIL:
                return Material.CHIPPED_ANVIL;
            case CHIPPED_ANVIL:
                return Material.DAMAGED_ANVIL;
            case DAMAGED_ANVIL:
                return null; // 已经是最低状态
            default:
                return null;
        }
    }
}