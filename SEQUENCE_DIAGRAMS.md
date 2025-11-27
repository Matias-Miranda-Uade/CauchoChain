# Diagramas de Secuencia - Transacciones de Tokens

## Diagrama 1: Creación de Token

```
Usuario            TokenPanel         BlockChain      TokenRegistry      Wallet(system)
  │                    │                  │                │                 │
  ├─ Crear Token ─────>│                  │                │                 │
  │ (BTC, 1000)        │                  │                │                 │
  │                    ├─ createToken()──>│                │                 │
  │                    │                  ├─ new Token()──>│                 │
  │                    │                  │                ├─ addToken()     │
  │                    │                  │                │                 │
  │                    ├────────────────────────────────────────────>        │
  │                    │ assignTokensToSystem(tokenId, 1000)                │
  │                    │                  │                │  updateBalance  │
  │                    │                  │                │  ("system", 1000)
  │                    │                  │                │                 │
  │<─ "Token Creado"──│                  │                │                 │
  │   exitosamente"    │                  │                │                 │

RESULTADO:
  Token("BTC") {
    totalSupply: 1000,
    balances: { "addr_system": 1000 }
  }
```

---

## Diagrama 2: Transferencia de Token

```
Usuario          WalletPanel        TokenRegistry      Transaction      Blockchain
  │                  │                   │                  │              │
  ├─ Selecciona ────>│                   │                  │              │
  │ from: system     │                   │                  │              │
  │ to: Principal    │                   │                  │              │
  │ amount: 100      │                   │                  │              │
  │                  │                   │                  │              │
  ├─ "Enviar" ──────>│                   │                  │              │
  │                  ├─ transferTokenWithTransaction()       │              │
  │                  │   ├─ Validar balance                │              │
  │                  │   │  (system tiene 1000 >= 100?) ✓   │              │
  │                  │   │                   │              │              │
  │                  │   ├─ transfer()      │              │              │
  │                  │   │ ├─ balances["system"] = 900     │              │
  │                  │   │ └─ balances["principal"] = 100  │              │
  │                  │   │                   │              │              │
  │                  │   ├───────────────────────────────────> new Transaction()
  │                  │   │                   │        from: "addr_system"  │
  │                  │   │                   │        to: "addr_principal"│
  │                  │   │                   │        amount: 100         │
  │                  │   │                   │        desc: "TOKEN:BTC:100"
  │                  │   │                   │              │              │
  │                  │   │                   │              ├──────────────>
  │                  │   │                   │              │ createTransaction()
  │                  │   │                   │              │              │
  │                  │<─ return true ───────│              │              │
  │                  │                      │              │              │
  │<─ "Éxito...... pendiente de minería"──│              │              │

RESULTADO:
  Token("BTC").balances:
    "addr_system": 900
    "addr_principal": 100
  
  blockchain.pendingTransactions:
    [ Transaction(system→principal, 100, "TOKEN:BTC:100") ]
```

---

## Diagrama 3: Minería de Bloque con Transacciones de Token

```
Usuario          MinerPanel         Miner          Blockchain      TokenRegistry
  │                  │                 │               │                │
  ├─ "Iniciar" ────>│                 │               │                │
  │ Minería         │                 │               │                │
  │                 ├─ startMining()  │               │                │
  │                 │                 │               │                │
  │                 ├─ Thread loop ───┤               │                │
  │                 │ (cada 1s)       │               │                │
  │                 │                 │               │                │
  │                 │  if (pendingTx)│               │                │
  │                 │                 ├─ mine()───────>               │
  │                 │                 │ ├─ new Block()               │
  │                 │                 │ │  con transacciones        │
  │                 │                 │ │                            │
  │                 │                 │ ├─ Resolve PoW:             │
  │                 │                 │ │ - Mientras hash no válido  │
  │                 │                 │ │   incrementa nonce         │
  │                 │                 │ │   recalcula hash           │
  │                 │                 │ │ - Cuando válido, sale loop│
  │                 │                 │ │                            │
  │                 │                 │ ├─ Add a chain             │
  │                 │                 │ ├─ Clear pending Tx        │
  │                 │                 │                            │
  │                 │                 │ ✨ NUEVO:                  │
  │                 │<─ return ───────┤                            │
  │                 │                 │                            │
  │                 ├─────────────────────────────────────>        │
  │                 │ syncBalancesFromBlockchain()                │
  │                 │                 │               │            │
  │                 │                 │               │  Procesa transacciones
  │                 │                 │               │  del bloque minado:
  │                 │                 │               │
  │                 │                 │               ├─ Para cada Token:
  │                 │                 │               │   ├─ Para cada Bloque:
  │                 │                 │               │   │  └─ Para cada Tx:
  │                 │                 │               │   │     Si "TOKEN:BTC"
  │                 │                 │               │   │     actualiza balance
  │                 │                 │               │   │
  │                 │                 │               ├─ Token.updateBalance()
  │                 │                 │               │  system: 900
  │                 │                 │               │  principal: 100
  │                 │                 │               │
  │                 │<────────────────────────────────┤
  │                 │                                  │
  │<─ "Bloque minado"                                 │

RESULTADO:
  Blockchain.chain:
    [ GenesisBlock, ... PreviousBlocks..., NewBlock ]
  
  NewBlock.transactions:
    [ Transaction(system→principal, 100, "TOKEN:BTC:100"),
      Transaction(null→miner, 50, null)  // Recompensa
    ]
  
  Token("BTC").balances:
    "addr_system": 900  ✓ Actualizado
    "addr_principal": 100  ✓ Actualizado
  
  blockchain.pendingTransactions: []  // Vacío
```

---

## Diagrama 4: Quema de Token

```
Usuario          TokenPanel         Token          Blockchain      TokenRegistry
  │                  │               │                 │                │
  ├─ Selecciona ────>│               │                 │                │
  │ Principal        │               │                 │                │
  │ Quemar: 20 BTC   │               │                 │                │
  │                  │               │                 │                │
  ├─ "Quemar" ──────>│               │                 │                │
  │                  │               │                 │                │
  │                  ├─ Validar balance                │                │
  │                  │  (principal tiene 100 >= 20) ✓  │                │
  │                  │               │                 │                │
  │                  ├─ burn()──────>│                 │                │
  │                  │               ├─ balances["principal"] = 80      │
  │                  │               ├─ totalSupply = 980 (era 1000)    │
  │                  │               │                 │                │
  │                  ├───────────────────────────────────> new Transaction()
  │                  │               │        from: "addr_principal"    │
  │                  │               │        to: "BURN"               │
  │                  │               │        amount: 20               │
  │                  │               │        desc: "BURN:BTC:20"      │
  │                  │               │                 │                │
  │                  │               │                 ├──────────────>│
  │                  │               │                 │ createTransaction()
  │                  │               │                 │                │
  │<─ "Tokens quemados exitosamente"                  │                │

RESULTADO DESPUÉS DE MINAR:
  Token("BTC"):
    totalSupply: 980  (reducido de 1000)
    balances: {
      "addr_system": 900,
      "addr_principal": 80,  ✓ Actualizado (100 - 20)
      "addr_secundaria": 30
    }
  
  blockchain.chain incluye:
    [ ... Block con Transaction(principal→BURN, 20, "BURN:BTC:20") ]
```

---

## Diagrama 5: Actualización de UI en Wallets

```
Timer (cada 5s)
  │
  ├─ updateWalletsList()
  │
  ├─ ✨ syncBalancesFromBlockchain()
  │  ├─ Para cada Token:
  │  │  ├─ Crear map vacío
  │  │  ├─ Procesar blockchain:
  │  │  │  ├─ Para cada bloque:
  │  │  │  │  └─ Para cada transacción:
  │  │  │  │     ├─ Si "TOKEN:BTC:100"
  │  │  │  │     │  ├─ balance[from] -= 100
  │  │  │  │     │  └─ balance[to] += 100
  │  │  │  │     └─ Si "BURN:BTC:20"
  │  │  │  │        └─ balance[from] -= 20
  │  │  ├─ Token.updateBalance() con nuevo map
  │  │  └─ Resultado final: Token.balances actualizado
  │  │
  │  └─ Todos los tokens sincronizados
  │
  ├─ Para cada Wallet:
  │  ├─ Obtener dirección
  │  ├─ Para cada Token:
  │  │  └─ balance = Token.getBalance(dirección)
  │  └─ Actualizar tabla
  │
  └─ UI actualizada con balances correctos

ANTES: 
  Tabla Wallets:
    Principal | 0 BTC

DESPUÉS (después de sincronizar):
  Tabla Wallets:
    Principal | 100 BTC  ✓
```

---

## Diagrama 6: Estado Completo del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    BLOCKCHAIN PERSISTENTE                    │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ BLOQUES MINADOS (Inmutables)                         │  │
│  │                                                       │  │
│  │ Block #0 (Genesis)                                  │  │
│  │   └─ Creado automáticamente                          │  │
│  │                                                       │  │
│  │ Block #1                                            │  │
│  │   ├─ Tx: system → principal, 100 BTC               │  │
│  │   └─ Tx: null → miner, 50 (recompensa)             │  │
│  │                                                       │  │
│  │ Block #2                                            │  │
│  │   ├─ Tx: principal → secundaria, 30 BTC            │  │
│  │   └─ Tx: null → miner, 50 (recompensa)             │  │
│  │                                                       │  │
│  │ Block #3                                            │  │
│  │   ├─ Tx: principal → BURN, 20 BTC                  │  │
│  │   └─ Tx: null → miner, 50 (recompensa)             │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ TRANSACCIONES PENDIENTES (No minadas aún)           │  │
│  │   [ ]  (vacío después de minar)                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    TOKEN REGISTRY                           │
│                                                              │
│  Token: BTC                                                 │
│  ├─ id: "uuid-1234"                                        │
│  ├─ name: "Bitcoin"                                        │
│  ├─ symbol: "BTC"                                          │
│  ├─ creator: "SYSTEM"                                      │
│  ├─ totalSupply: 980  (1000 - 20 quemados)               │
│  └─ balances:                                              │
│     ├─ "addr_system": 900                                  │
│     ├─ "addr_principal": 80                                │
│     └─ "addr_secundaria": 30                               │
│                                                              │
│  Token: ETH                                                 │
│  ├─ (similar structure)                                    │
│  └─ (...)                                                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    WALLETS (UI)                             │
│                                                              │
│  system:      900 BTC                                       │
│  Principal:   80 BTC                                        │
│  Secundaria:  30 BTC                                        │
│                                                              │
│  (Valores = Resultado de procesar blockchain)              │
└─────────────────────────────────────────────────────────────┘

Los valores en WALLETS son DERIVADOS del BLOCKCHAIN:
  Balance = Suma de transacciones donde address es destinatario
          - Suma de transacciones donde address es emisor
```

---

## Flujo de Validación de Datos

```
1. INVARIANTE: Suma de balances ≤ suministro total

   system (900) + principal (80) + secundaria (30) = 1010 > 980
   ❌ INCONSISTENTE si totalSupply no se decrement

   Pero si totalSupply se decrementó por quema:
   1000 - 20 (quemados) = 980
   900 + 80 + 30 = 1010  ← Error, algo está mal

   En realidad, los 980 deben estar distribuidos:
   system (900) + principal (80) + secundaria (0) = 980
   O después de quemar de principal:
   system (900) + principal (80) + secundaria (0) = 980

2. Si algo no suma:
   ├─ Verificar blockchain está íntegro (hashes válidos)
   ├─ Verificar transacciones en bloques son válidas
   ├─ Re-ejecutar syncBalancesFromBlockchain()
   └─ Los balances se recalculan desde la blockchain

3. Fuente de verdad:
   La blockchain es INMUTABLE y TOTAL
   Los balances se DERIVAN de la blockchain
   Nunca se modifican directamente
```

---

## Garantías del Sistema

✅ **Atomicidad**: Una transferencia es todo o nada
   - Si falla crear transacción, revierte la transferencia
   
✅ **Consistencia**: Los balances siempre son correctos
   - Se recalculan después de cada bloque minado
   - Se sincronizan cuando se accede a la UI
   
✅ **Durabilidad**: Una vez minado, es permanente
   - El bloque no se puede cambiar
   - La transacción quedó registrada para siempre
   
✅ **Aislamiento**: Las transacciones se procesan secuencialmente
   - Se mina un bloque a la vez
   - El orden está garantizado por la blockchain

✅ **Auditabilidad**: Historial completo
   - Cada movimiento está registrado
   - Se puede ver quién enviaba qué a quién y cuándo

