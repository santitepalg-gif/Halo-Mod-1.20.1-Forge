package com.halomod.entity;

/**
 * Estadísticas de cada tropa, tomadas de tus JSON de Bedrock (vida, velocidad, daño melee,
 * hitbox, rangos y ráfagas). El id es también el nombre de los assets:
 *   geo/entity/<id>.geo.json, animations/entity/<id>.animation.json, textures/entity/<id>.png
 *
 * ELITE_MAYOR y HUNTER_MENOR/HUNTER: ver README (valores propios donde tus JSON no los traían).
 */
public enum CovenantSpec {
    //               id              vida    vel   melee  ancho  alto  follow weapon                      ráfaga(min,max) intervalo cdRáfaga rango meleeR cobarde escudo% escudoTicks kbRes reach
    ELITE_MENOR("elite_menor",       260,  0.39,   30,   0.7f, 1.9f,  45, WeaponType.PLASMA_RIFLE,    9, 15,  2, 10,  45, 4, false, 0.00f,   0, 0.0, 1.0f),
    ELITE_MAYOR("elite_mayor",       390,  0.40,   40,   0.7f, 2.0f,  45, WeaponType.PLASMA_RIFLE,    9, 15,  2, 10,  45, 4, false, 0.00f,   0, 0.2, 1.0f),
    GRUNT("grunt_menor",             100,  0.27,   15,   0.7f, 1.45f, 45, WeaponType.PLASMA_PISTOL,   3,  7,  5, 10,  45, 8, true,  0.00f,   0, 0.0, 1.0f),
    JACKAL("jackal",                 115,  0.35,   15,   0.6f, 1.6f,  45, WeaponType.PLASMA_PISTOL,   6,  9,  5, 12,  45, 6, false, 0.40f,  40, 0.0, 1.0f),
    BRUTE_MENOR("brute_menor",       275,  0.31,   40,   0.72f, 1.9f, 45, WeaponType.PLASMA_RIFLE,    7, 13,  3, 10,  45, 7, false, 0.00f,   0, 0.0, 1.0f),
    HUNTER_MENOR("hunter_menor",     950,  0.25,   50,   1.6f, 2.6f, 100, WeaponType.ASSAULT_CANNON,  5, 15,  6, 20,  75, 7, false, 0.25f, 120, 1.0, 1.6f),
    HUNTER("hunter",                1680,  0.25,  140,   1.6f, 2.6f, 100, WeaponType.ASSAULT_CANNON,  1,  8,  6, 20,  75, 7, false, 0.25f, 120, 1.0, 1.6f);

    public final String id;
    public final double health, speed, melee;
    public final float width, height;
    public final double followRange;
    public final WeaponType weapon;
    public final int burstMin, burstMax, burstInterval, burstCooldown;
    public final double range, meleeRange;
    public final boolean cowardly;
    public final float shieldChance;
    public final int shieldTicks;
    public final double knockbackResistance;
    public final float reach;

    CovenantSpec(String id, double health, double speed, double melee, float width, float height,
                 double followRange, WeaponType weapon, int burstMin, int burstMax, int burstInterval,
                 int burstCooldown, double range, double meleeRange, boolean cowardly,
                 float shieldChance, int shieldTicks, double knockbackResistance, float reach) {
        this.id = id;
        this.health = health;
        this.speed = speed;
        this.melee = melee;
        this.width = width;
        this.height = height;
        this.followRange = followRange;
        this.weapon = weapon;
        this.burstMin = burstMin;
        this.burstMax = burstMax;
        this.burstInterval = burstInterval;
        this.burstCooldown = burstCooldown;
        this.range = range;
        this.meleeRange = meleeRange;
        this.cowardly = cowardly;
        this.shieldChance = shieldChance;
        this.shieldTicks = shieldTicks;
        this.knockbackResistance = knockbackResistance;
        this.reach = reach;
    }
}
