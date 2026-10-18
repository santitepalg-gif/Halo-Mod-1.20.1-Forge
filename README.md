# Halo Covenant (fan mod) — Forge 1.20.1 + GeckoLib 4

Mod Java con las mismas tropas y armas de tu addon de Bedrock, usando **solo tus assets**
(este proyecto no incluye ningún modelo, textura ni animación: tú los pones).

## Qué trae

| Qué | Detalle |
|---|---|
| Tropas | Elite menor, Elite mayor, Grunt menor, Jackal, Brute menor, Hunter menor (950 HP) y Hunter (1680 HP). Una sola clase (`CovenantEntity`) con un `CovenantSpec` por tropa |
| Armas | Plasma Rifle, Plasma Pistol, Cañón de asalto (item GeckoLib con animación `fire`; mantén clic derecho para disparar) |
| Phantom | Baja del cielo, abre puertas laterales, suelta 8 tropas (Elite mayor, Elite menor, Brute, 2 Grunts, 2 Jackals, Hunter menor), cierra puertas y se va |
| Comportamiento | Ráfagas a distancia, melee de cerca, Grunts que huyen al verte cerca, escudos de Jackal/Hunter contra proyectiles, sin fuego amigo entre Covenant |

Las estadísticas (vida, velocidad, daño, hitbox, rango, tamaño de ráfaga) salen de tus JSON.
Todo está en `entity/CovenantSpec.java` y `entity/WeaponType.java`.

**Valores que no estaban en tus JSON (los puse yo, cámbialos a gusto):** vida/velocidad/daño del Elite mayor
(390 / 0.40 / 40), daño de los proyectiles por arma, tropas del Phantom, alturas y tiempos de la secuencia.

## Dónde poner tus assets

Convierte tus archivos de Bedrock y copia en `src/main/resources/assets/halo/`:

| Bedrock | Java (GeckoLib) |
|---|---|
| `models/entity/<x>.geo.json` | `geo/entity/<id>.geo.json` |
| `animations/<x>.animation.json` | `animations/entity/<id>.animation.json` |
| `textures/entity/<x>.png` | `textures/entity/<id>.png` |
| geo/anim/textura de cada arma | `geo/item/<id>.geo.json`, `animations/item/<id>.animation.json`, `textures/item/<id>.png` |

Los `animation controllers` de Bedrock **no se portan**: la lógica de estados ya está en Java.
Solo necesito que tus animaciones tengan estas claves (renombra las tuyas):

- **Tropas** (`id` = `elite_menor`, `elite_mayor`, `grunt_menor`, `jackal`, `brute_menor`, `hunter_menor`, `hunter`):
  `idle`, `walk`, `run`, `shoot`, `melee`, `block`, `death`
- **Phantom** (`id` = `phantom`): `arrive`, `open_doors`, `doors_open_idle`, `close_doors`, `depart`
- **Armas** (`id` = `plasma_rifle`, `plasma_pistol`, `assault_cannon`): `idle`, `fire`

El formato geo de Bedrock 1.12.0 funciona directo en GeckoLib 4; si tus geo son de formato más viejo
(1.8 o 1.10, como los de entidades antiguas) conviértelos con Blockbench a "GeckoLib Animated Model".

## Probar

1. `gradle wrapper --gradle-version 8.8` (o copia el wrapper del MDK 1.20.1 de Forge), luego `./gradlew runClient`.
2. Creativo → pestaña "Halo Covenant": huevos de cada tropa, del Phantom y las 3 armas.
   El Phantom también con `/summon halo:phantom ~ ~ ~` (el punto donde lo invocas es el suelo objetivo).
3. Para sacar el .jar: `./gradlew build` → `build/libs/halo-0.1.0.jar` (requiere GeckoLib 4.4+ en la carpeta mods).

## Estado

Escrito contra las APIs de Forge 47.3 / GeckoLib 4.4.9, **sin compilar ni probar en juego** (sin tus assets no se puede ver nada).
Si el primer `build` marca algún error de API, pásame el log y lo corrijo.
