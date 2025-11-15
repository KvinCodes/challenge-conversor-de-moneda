# 🪙 Conversor de Monedas en Java  
Proyecto desarrollado como parte del **Programa ONE – Oracle Next Education**, cumpliendo con las fases guiadas del desafío oficial.  
Incluye integración con API real de tasas de cambio, uso de caché local, menú interactivo avanzado y manejo seguro de datos.

---

## 📌 Índice

- [Descripción General](#descripción-general)
- [Características Principales](#características-principales)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Funcionamiento del Programa](#funcionamiento-del-programa)
- [API de Tasas de Cambio](#api-de-tasas-de-cambio)
- [Instalación y Configuración](#instalación-y-configuración)
- [Ejecución](#ejecución)
- [Estructura del Menú Interactivo](#estructura-del-menú-interactivo)
- [Sistema de Caché](#sistema-de-caché)
- [Aprendizajes del Desafío](#aprendizajes-del-desafío)
- [Capturas (Opcional)](#capturas-opcional)
- [Autor](#autor)

---

## 🧠 Descripción General

Este proyecto es un **Conversor de Monedas en Java**, capaz de convertir valores entre diversas monedas internacionales utilizando tasas de cambio obtenidas desde una API real.  

El programa funciona desde la consola e incluye:
- un **menú interactivo profesional**,  
- selección manual o guiada de monedas,  
- cálculos avanzados,  
- manejo de errores,  
- uso de caché para optimizar consultas,  
- pruebas varias de funcionamiento.

Fue desarrollado siguiendo todas las fases del Challenge ONE, desde consumo de API hasta la interacción con usuario final.

---

## ✨ Características Principales

✔️ Consumo de API real usando Java + Gson  
✔️ Conversión directa e inversa entre monedas  
✔️ Submenús avanzados para elegir origen y destino  
✔️ Conversión personalizada ingresada por el usuario  
✔️ Opción de pares rápidos predefinidos  
✔️ Opción para consultar tasas filtradas desde `/latest`  
✔️ Sistema de caché para evitar llamadas repetidas  
✔️ Formato numérico profesional  
✔️ Manejo de errores y validaciones de entrada  
✔️ Código modular, limpio y escalable  

---


Cada clase cumple una responsabilidad clara:
- **Conversor** → Interfaz con el usuario + lógica principal  
- **ExchangeRateApiClient** → Comunicación con la API  
- **SimpleCache** → Cache local de tasas  
- **Config** → Lectura de configuraciones  

---

## 🛠 Tecnologías Utilizadas

- **Java 17+**  
- **Gson** para leer JSON  
- **Java HttpClient** para llamadas HTTP  
- **Java Collections / Map / Optional**  
- **API ExchangeRate-API u otra similar**  

---

## ⚙️ Funcionamiento del Programa

El conversor ofrece distintas formas de convertir monedas:

### 🔹 1. Pares rápidos
Conversión instantánea entre pares comunes como:
- USD → EUR  
- USD → JPY  
- USD → MXN  
(Entre otros)

### 🔹 2. Par personalizado
El usuario escribe:
- Base: USD
- Destino: BRL


### 🔹 3. Conversión interactiva avanzada  
Permite elegir monedas desde un menú y evitar errores.

### 🔹 4. Tasas filtradas desde la API  
Muestra solo monedas importantes: USD, EUR, JPY, MXN, COP, CRC, etc.

### 🔹 5. Conversión inversa  
Si la API no provee una tasa directa, se calcula su inversa matemáticamente.

---

## 🌐 API de Tasas de Cambio

El programa usa una API estilo:
```https://v6.exchangerate-api.com/v6/TU_API_KEY/latest/USD```


Las tasas obtenidas se almacenan en caché por 1 hora para optimizar el rendimiento.

---

## 🚀 Instalación y Configuración

### 1️⃣ Clonar el repositorio
```git clone https://github.com/KvinCodes/challenge-conversor-de-moneda.git```


### 2️⃣ Agregar la API Key  
Usar **una de estas opciones**:

#### ✔ Opción A — Variable de entorno
EXR_API_KEY=TU_API_KEY


#### ✔ Opción B — Archivo config.properties  
config/config.properties

Contenido:
- EXR_API_KEY=TU_API_KEY

  
### 3️⃣ Agregar la librería Gson  
Si usas IntelliJ o VSCode con Maven/Gradle, agrega:

**Maven**
````xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>

▶ Ejecución

Desde terminal:
cd src
javac principal/*.java
java principal.Conversor
