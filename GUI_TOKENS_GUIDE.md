# 🎨 Guía Visual del Sistema de Tokens - PirgaChain

## Interfaz Gráfica del Panel de Tokens

### Vista General
```
┌─────────────────────────────────────────────────────────────────────────────┐
│ MainWindow - CauchoChain - Blockchain Simulator                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ [Vista Blockchain] [Tokens] [Wallets] [Minería] [Transacciones] [Config.]   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                               │
│ ┌─── Crear Nuevo Token ─────────────────────────────────────────────────┐   │
│ │ Nombre: [Mi Moneda    ] Símbolo: [MIM  ] Suministro: [1000      ]    │   │
│ │ Creador: [SYSTEM              ] [Crear Token] ✓                      │   │
│ └─────────────────────────────────────────────────────────────────────┘   │
│                                                                               │
│ ┌─── Tokens Registrados ────┐  ┌──────── Detalles del Token ────────┐     │
│ │ Nombre │ Símbolo │ ... │ Creador         │  │ === DETALLES ===     │     │
│ ├─────────────────────────────┤  │ ID: 550e8400-e29b-41d4-a716-...   │     │
│ │ Dogecoin│ DOGE   │ 1000000 │  │ Nombre: Dogecoin                 │     │
│ │ Bitcoin │ BTC    │ 21000000│  │ Símbolo: DOGE                    │     │
│ │ Ethereum│ ETH    │ 1000000 │  │ Suministro: 1000000              │     │
│ │ Bitcoin │ BTC    │ 21000000│  │ Creador: SYSTEM                  │     │
│ │         │        │         │  │ Creado: 2024-01-15 10:30:45      │     │
│ │         │        │         │  │                                  │     │
│ │                             │  │ === SALDOS ===                   │     │
│ │ [Actualizar]                │  │ SYSTEM: 900000                   │     │
│ └─────────────────────────────┘  │ USUARIO_A: 100000                │     │
│                                   │ USUARIO_B: 500000                │     │
│                                   │ USUARIO_C: 50000                 │     │
│                                   └──────────────────────────────────┘     │
│                                                                               │
│                              [Actualizar]                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📝 Flujo de Uso

### Paso 1: Crear un Token
```
Usuario abre la pestaña "Tokens"
        ↓
Completa el formulario:
  • Nombre: "Dogecoin"
  • Símbolo: "DOGE"
  • Suministro: 1000000
  • Creador: "SYSTEM"
        ↓
Haz clic en "Crear Token"
        ↓
Se validan los datos
        ↓
Se crea el token en TokenRegistry
        ↓
Aparece mensaje de éxito con ID del token
        ↓
La tabla se actualiza automáticamente
```

### Paso 2: Ver Detalles
```
Usuario selecciona una fila en la tabla
        ↓
El panel derecho "Detalles del Token" se actualiza
        ↓
Muestra:
  - Información del token
  - Tabla completa de balances
  - Quién posee cuántos tokens
```

---

## 🔄 Operaciones Disponibles

### 1️⃣ Crear Token
**Entrada:**
- Nombre: String (ej: "Dogecoin")
- Símbolo: String (ej: "DOGE")
- Suministro: Long (ej: 1000000)
- Creador: String (dirección/nombre del creador)

**Resultado:**
- Nuevo token creado y registrado
- Creador recibe todo el suministro inicial
- ID único generado

**Código:**
```java
Token token = blockchain.tokenRegistry.createToken(
    "Dogecoin",
    "DOGE",
    1000000,
    "SYSTEM"
);
```

---

### 2️⃣ Transferir Tokens
**Operación:** Mover tokens de una dirección a otra

**Entrada:**
- Token ID
- Dirección origen
- Dirección destino
- Cantidad

**Validaciones:**
- ✓ Dirección origen tiene suficiente saldo
- ✓ Cantidad > 0
- ✓ Token existe

**Código:**
```java
boolean exito = blockchain.tokenRegistry.transferToken(
    tokenId,
    "USUARIO_A",
    "USUARIO_B",
    1000
);
```

**Antes:**
- USUARIO_A: 5000 DOGE
- USUARIO_B: 2000 DOGE

**Después:**
- USUARIO_A: 4000 DOGE
- USUARIO_B: 3000 DOGE

---

### 3️⃣ Acuñar (Mint) Tokens
**Operación:** Crear nuevos tokens (solo el creador)

**Entrada:**
- Token ID
- Dirección destino
- Cantidad
- Dirección del llamador (debe ser el creador)

**Validaciones:**
- ✓ Quien llama es el creador
- ✓ Cantidad > 0

**Código:**
```java
boolean exito = blockchain.tokenRegistry.mintToken(
    tokenId,
    "USUARIO_A",
    500000,
    "SYSTEM"  // Solo el creador
);
```

**Resultado:**
- Se crean nuevos tokens
- Se asignan a USUARIO_A
- Aumenta el suministro total

---

### 4️⃣ Quemar (Burn) Tokens
**Operación:** Eliminar tokens (reduce el suministro total)

**Entrada:**
- Token ID
- Dirección que quema
- Cantidad

**Código:**
```java
boolean exito = blockchain.tokenRegistry.burnToken(
    tokenId,
    "USUARIO_A",
    100000
);
```

**Resultado:**
- Se restan tokens de USUARIO_A
- Disminuye el suministro total

---

## 📊 Estructura de Datos

### Token
```
Token
├── id: "550e8400-e29b-41d4-a716-446655440000" (UUID)
├── name: "Dogecoin"
├── symbol: "DOGE"
├── totalSupply: 1000000 (long)
├── creator: "SYSTEM"
├── createdAt: 1705329045000 (timestamp milisegundos)
└── balances: Map<String, Long>
    ├── "SYSTEM" → 450000
    ├── "USUARIO_A" → 350000
    └── "USUARIO_B" → 200000
```

### TokenRegistry
```
TokenRegistry
├── tokens: Map<String, Token>
│   ├── "550e8400-..." → Token(Dogecoin)
│   ├── "660e8400-..." → Token(Bitcoin)
│   └── "770e8400-..." → Token(Ethereum)
└── tokenIds: List<String>
    └── [550e8400-..., 660e8400-..., 770e8400-...]
```

---

## 🎯 Casos de Uso

### Caso 1: Sistema de Gobernanza
```
1. Crear token "GobernanzaChain" (GOV)
2. Distribuir a votantes
3. Usar balance como peso de voto
```

### Caso 2: Economía de Juego
```
1. Crear token "GoldCoin" (GOLD) para recompensas
2. Mineros ganan GOLD al validar bloques
3. Usuarios intercambian GOLD por bienes
```

### Caso 3: Sistema de Lealtad
```
1. Crear token "LoyaltyPoints" (LP)
2. Acuñar LP al hacer transacciones
3. Quemar LP al usar beneficios
```

### Caso 4: Moneda Estable
```
1. Crear token "StableCoin" (STABLE)
2. Control centralizado del creador
3. Acuñación bajo demanda
```

---

## ⚙️ Métodos del TokenPanel

### `createNewToken(String name, String symbol, long totalSupply, String creator)`
- Crea un nuevo token validando datos
- Muestra diálogo de éxito/error
- Actualiza la tabla

### `showTokenDetails()`
- Se ejecuta cuando seleccionas un token
- Muestra información completa
- Actualiza panel derecho

### `refreshTokenList()`
- Actualiza la tabla de tokens
- Se llama cada 3 segundos automáticamente
- Se puede activar manualmente

---

## 🚀 Integración con otras partes

### Con Blockchain
```java
blockchain.tokenRegistry.createToken(...)  // Acceso directo
```

### Con Logger
```java
blockchain.getLogger().info("Token creado: ...");
```

### Con GUI
```java
tabs.add("Tokens", new TokenPanel(blockchain));
```

---

## ✨ Características Especiales

1. **Validación Automática**: Todos los datos se validan antes de operaciones
2. **Actualización en Tiempo Real**: Tabla se actualiza cada 3 segundos
3. **Interfaz Intuitiva**: Campos claramente etiquetados y organizados
4. **Manejo de Errores**: Diálogos informativos en caso de error
5. **Información Detallada**: Panel lateral muestra balances completos
6. **Identidad Única**: Cada token tiene un UUID único e inmutable

---

## 🔐 Seguridad

- ✅ Solo el creador puede acuñar tokens
- ✅ Se valida saldo antes de transferencias
- ✅ Se valida existencia de tokens
- ✅ Campos de entrada sanitizados
- ✅ Todas las operaciones registradas en logger

---

## 📈 Próximas Mejoras

- [ ] Transferencias en GUI (dragAndDrop)
- [ ] Historial completo de transacciones
- [ ] Gráficas de distribución
- [ ] Sistema de aprobaciones (allowance)
- [ ] Decimales para precisión
- [ ] Estándares ERC-20 completos
- [ ] Sistema de fees por transferencia
- [ ] Whitelist/Blacklist de direcciones

