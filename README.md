# AnvilStorm
把铁砧扔出去尽情的砸人吧）
```
---------------------------------------------------------------
                       _  _     _                               
   /\                 (_)| |   | |    _                         
  /  \   ____   _   _  _ | |    \ \  | |_    ___    ____  ____  
 / /\ \ |  _ \ | | | || || |     \ \ |  _)  / _ \  / ___)|    \ 
| |__| || | | | \ V / | || | _____) )| |__ | |_| || |    | | | |
|______||_| |_|  \_/  |_||_|(______/  \___) \___/ |_|    |_|_|_|
AnvilStorm - 铁砧风暴来袭！
---------------------------------------------------------------
```
这个插件可以把你手上的铁砧抛出去，而你只需要拿着铁砧蹲下就行
<br>
配置文件：
```
# AnvilStorm 插件配置文件

# 铁砧投掷功能配置
anvil-throw:
  enabled: true  # 是否启用功能
  max-charge-time: 2.0  # 最大蓄力时间（秒）
  max-velocity: 2.0  # 最大速度
  log-level: 0  # 日志级别：0=关闭, 1=最小, 2=最大
  charge-bar:
    type: "exp"  # 蓄力条类型：exp=经验条, bossbar=Boss血条, actionbar=动作栏
    color: "green"  # 蓄力条颜色（当type为bossbar和actionbar时生效）：red=红色, green=绿色, blue=蓝色, yellow=黄色, purple=紫色, white=白色
  damage:
    type: "auto"  # 伤害类型：auto=自动计算，number=固定值
    fixed-damage: 20.0  # 固定伤害值（当type为number时生效）
    max-damage: 40.0  # 最大伤害值（当type为auto时生效）
    radius: 1.5  # 伤害范围（格）
```
