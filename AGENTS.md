# Agent Instructions: Amor Horneado (Bakery Central) - PROYECTO INTEGRAL

## 1. Perfil del Agente
Eres un Desarrollador Senior de Android especializado en Kotlin, Jetpack Compose y Arquitectura MVVM. Tu misión es mantener la excelencia técnica y visual de la App "Amor Horneado", asegurando coherencia entre los módulos de inventario, costos y ventas.

## 2. Identidad Visual (Premium Bakery Dark Mode)
- **Fondo:** `#1A120B` (Espresso profundo) - Aplicado en `MaterialTheme.colorScheme.background`.
- **Tarjetas/Superficies:** `#2D2013` (Chocolate oscuro) - Usado en `IngredientItem` y componentes de superficie.
- **Acento Principal:** `#F57C00` (Naranja Panadería / BakeryOrange) - Utilizado para iconos, botones principales, bordes enfocados y montos financieros destacados.
- **Estados Positivos/Pagados:** `#4CAF50` (Verde Éxito) - Usado para estados de "PAGADO" y montos de abono.
- **Estados Críticos:** `CriticalRed` para stock bajo (≤ 2 unidades) y alertas.
- **Textos:**
  - Títulos Principales: `42.sp`, `ExtraBold`, Blanco.
  - Montos en Detalle: `36.sp`, `FontWeight.Black`, `BakeryOrange`.
  - Subtítulos/Cuerpo: `#B0BEC5` o `Color.Gray`.
  - Labels de Navegación: `10.sp`, `Medium`.

## 3. Arquitectura y Módulos Core

### A. Inventario e Insumos
- **Pantalla:** `IngredientsScreen` con búsqueda integrada.
- **Stock:** Indicadores de color dinámicos (Naranja vs Rojo Crítico). Unidades normalizadas en mayúsculas (KG, LTS, UNID).

### B. Dashboard & Analytics
- **KPIs:** Ventas por periodos y alertas de stock bajo.
- **Producción:** Visualización de stock final con formato dual `x[Cant] + x[Cant]P` (Portciones) cuando aplica.
- **Navegación Rápida:** Botón "Gestionar" en alertas redirige directamente al editor correspondiente.

### C. Finanzas y Deudas (Módulo Debts)
- **Estructura:** Tres sub-pestañas: "Pendientes", "Abonos" y "Pagados".
- **Búsqueda:** Campo de búsqueda estandarizado que filtra por nombre de cliente en todas las sub-pestañas.
- **Detalles:** Diálogos interactivos que muestran monto en $, conversión a Bs (según tasa del día del registro), fecha/hora y concepto/producto.

### D. Módulo de Producción y Carrito (Nuevo)
- **Venta Flexible:** Soporte para productos completos y porciones (🍰 vs 🎂).
- **Lógica de Carrito:**
  - Productos sin porciones: Se agregan directamente al carrito desde la lista.
  - Productos con porciones: Requieren confirmación en el `SellDialog`.
- **Gestión de Pagos Avanzada:**
  - **Múltiples Métodos:** Soporte para seleccionar varios métodos de pago en una sola transacción (ej. Zelle + Efectivo).
  - **Nuevos Métodos:** Integración de Zelle, Binance y Transferencia Bancaria.
- **SellDialog:** Rediseño minimalista centrado en el precio en dólares (**36.sp**) con referencia en Bs (**REF**), selector de formato con "o" central estilizada en un círculo y bloqueo inteligente de botones +/- según stock.

## 4. Estándares de Desarrollo
1. **Estandarización de Pantallas:** Todas las pantallas de gestión deben seguir el orden:
   - `Spacer(64.dp)` -> Título (42.sp) -> Subtítulo -> `Spacer(40.dp)` -> Barra de Búsqueda.
2. **Navegación Condicional:** La `bottomBar` **SOLO** es visible en `dashboard`, `ingredients`, `recipes`, `production` y `settings`. Se oculta en editores y detalles para evitar bucles.
3. **Diálogos de Detalle:** Deben usar `HorizontalDivider` (0.3f alpha) para separar secciones (Header, Info, Monto Final).
4. **Registro de Pagos:** Incluir botón "TODO" en el campo de monto para autocompletar la deuda pendiente. Soportar tanto punto como coma como separador decimal.
5. **Precisión Financiera:** Siempre mostrar la tasa de cambio aplicada al momento de la transacción en los detalles de abono.

## 5. Glosario de Componentes Visuales
- **Búsqueda:** `OutlinedTextField` con `BakeryOrange` (focused), `RoundedCornerShape(12.dp)` y `leadingIcon` de lupa.
- **Status Badge (Pagado):** `Box` con `border(1.dp, Color(0xFF4CAF50))` y `RoundedCornerShape(4.dp)`.
- **Botones de Acción:** `BakeryOrange` con texto en blanco; altura estándar de 36.dp para botones secundarios dentro de tarjetas.
- **Separadores:** `HorizontalDivider` con `thickness = 0.5.dp` y `color = Color.DarkGray`.

---
*Nota: Priorizar la legibilidad y la respuesta táctil. Cada transacción financiera debe ser trazable con fecha, hora y tasa de cambio.*
