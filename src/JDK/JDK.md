# JVM vs JRE vs JDK — Complete Interview Revision Notes

---

## 1. The Big Picture

```
JDK (Java Development Kit)
 └── JRE (Java Runtime Environment)
      └── JVM (Java Virtual Machine)
```

| | Definition | Can Compile? | Can Run? | Used By |
|---|---|---|---|---|
| **JVM** | Spec + engine that executes bytecode | No | Yes (needs libs in practice) | Internally used by JRE |
| **JRE** | JVM + standard libraries + launcher | No | Yes | End users running Java apps |
| **JDK** | JRE + dev/compile/debug tools | Yes (`javac`) | Yes | Developers |

**One-liner:** JDK is for developers (compile + run), JRE is for end users (run only), JVM is the actual runtime engine that executes bytecode — platform-dependent in implementation, platform-independent in the bytecode it consumes. That's what gives Java "write once, run anywhere."

> Since Java 11, Oracle stopped shipping a standalone JRE bundle — you just get a JDK now. Conceptually the JRE layer still exists inside it.

---

## 2. How Java Code Flows (End-to-End)

```
.java file
   → javac (compiler, JDK only)
       → .class file (bytecode)
           → JVM ClassLoader loads it
               → Bytecode Verifier checks it
                   → Execution Engine (Interpreter + JIT)
                       → Native machine code
                           → OS / Hardware
```

**Steps:**
1. Write source code (`.java`)
2. `javac` compiles → bytecode (`.class`) — platform-independent, NOT machine code
3. JVM loads `.class` at runtime via **ClassLoader subsystem**
4. Bytecode passes **Verifier** (security & correctness checks)
5. **Execution Engine** runs it:
    - Interpreter reads bytecode line-by-line (fast startup, slow repetition)
    - **JIT Compiler** compiles "hot" code paths into native machine code
6. Native code executes on hardware via OS

---

## 3. JVM — Deep Dive

### What It Is
An **abstract specification**, not a single product. Implementations: HotSpot (Oracle/OpenJDK default), OpenJ9, GraalVM, Zing. The **spec** is platform-independent; every **implementation** is platform-specific native code.

### Why "Write Once, Run Anywhere" Works
`javac` produces bytecode (opcodes like `iload`, `invokevirtual`, `areturn`), not machine code. Each OS has its own JVM binary translating that same bytecode to native instructions. Portability lives at the **bytecode** layer.

### A) Class Loader Subsystem

**Loading — 3 hierarchical loaders:**

| Loader | Loads |
|---|---|
| **Bootstrap ClassLoader** | Native code; loads `java.base` classes (`Object`, `String`) from `<JAVA_HOME>/lib`. Shows as `null` if printed. |
| **Platform/Extension ClassLoader** | Platform modules / `ext` dir classes |
| **Application ClassLoader** | Your classpath, your compiled classes, dependency jars |

**Parent Delegation Model:** Child loader asks parent first before trying itself.
- Prevents class spoofing (a custom `java.lang.String` never overrides the real one)
- Ensures core classes are always trustworthy

Custom class loaders can be built by extending `ClassLoader` (used in Tomcat, plugin systems).

**Linking — 3 sub-steps:**

| Step | What Happens |
|---|---|
| **Verify** | Bytecode Verifier checks structural validity, access rules, no illegal casts/stack issues — a security boundary |
| **Prepare** | Memory allocated for static fields, set to **default values** (0, null, false) |
| **Resolve** | Symbolic references (names in constant pool) replaced with direct memory references |

**Initialization:** Static variables get **actual assigned values**; static blocks run top-to-bottom, only once, on first active use of the class (not merely referenced).

> **Trap Q:** "Does a static block run at compile time?" → No, runtime, during Initialization phase.

### B) Runtime Data Areas (Memory Model)

| Area | Scope | Contents | GC Managed? |
|---|---|---|---|
| **Method Area / Metaspace** | Shared | Runtime constant pool, field/method data, static vars, method bytecode | Yes |
| **Heap** | Shared | All objects, instance vars, arrays | Yes (main GC target) |
| **JVM Stack** | Per-thread | Stack frames per method call: local vars, operand stack, frame data | No — LIFO, popped on return |
| **PC Register** | Per-thread | Address of currently executing instruction | No |
| **Native Method Stack** | Per-thread | Supports native (JNI/C/C++) method calls | No |

**PermGen → Metaspace (Java 8 change):**
- Pre-Java 8: static/class metadata in **PermGen**, fixed size inside JVM memory → common `OutOfMemoryError: PermGen space`
- Java 8+: **Metaspace**, allocated from **native OS memory**, grows dynamically (bounded by `-XX:MaxMetaspaceSize` if set)

**Heap Structure (Generational GC):**
```
Heap
 ├── Young Generation
 │     ├── Eden Space        (new objects born here)
 │     ├── Survivor Space S0
 │     └── Survivor Space S1
 └── Old Generation (Tenured)  (long-lived objects promoted here)
```
- New objects → Eden
- Minor GC moves survivors between S0/S1 (copying collector)
- After surviving enough cycles (tenuring threshold) → promoted to Old Gen
- Old Gen cleaned by Major/Full GC — more expensive, longer pauses

**Stack Frame Internals:**
- Local variable array (params + local vars)
- Operand stack (working area, e.g., pushing two ints before `iadd`)
- Reference to runtime constant pool

### C) Execution Engine

| Component | Role |
|---|---|
| **Interpreter** | Executes bytecode instruction-by-instruction. Fast startup, slow on repeated code. |
| **JIT Compiler** | Profiles hot methods/loops, compiles to native machine code, caches for reuse (hence "HotSpot") |
| **C1 (Client Compiler)** | Quick, less optimized — fast startup |
| **C2 (Server Compiler)** | Slower compile, highly optimized — long-running server apps |
| **Tiered Compilation** (default since Java 8) | Interpreter → C1 → C2 progressively based on "hotness" |
| **Garbage Collector** | Automatic memory reclamation |

**GC Algorithms:**

| GC | Notes |
|---|---|
| Serial GC | Single-threaded, small apps |
| Parallel GC | Multi-threaded, throughput-focused |
| G1 (Garbage First) | Default since Java 9, region-based, balances throughput/pause time |
| ZGC / Shenandoah | Very low pause-time, huge heaps (Java 11+/15+) |

**JNI (Java Native Interface):** Bridges Java ↔ native C/C++ code. Supported by Native Method Stack + Native Method Libraries.

### JVM Interview Q&A

- **`-Xms` vs `-Xmx`?** → Initial heap size vs max heap size.
- **`StackOverflowError` cause?** → JVM Stack exceeded (deep/infinite recursion).
- **Heap `OutOfMemoryError` cause?** → Heap full, GC can't reclaim enough (leak or genuinely too many live objects).
- **Minor GC vs Major/Full GC?** → Minor cleans Young Gen (fast, frequent); Major/Full cleans Old Gen/whole heap (slow, "stop-the-world").
- **Where do String literals live?** → String objects in Heap; literals in **String Pool** (moved from PermGen/Method Area to Heap since Java 7, via interning).

---

## 4. JRE — Deep Dive

### What It Is
**JVM + Java Standard Class Libraries + supporting runtime files.** Everything needed to *run* compiled Java applications.

### Why JVM Alone Isn't Enough
JVM spec only defines *how to execute bytecode instructions* (`new`, `invokevirtual`, `areturn`). It has no built-in knowledge of `ArrayList`, `HashMap`, `Thread`, or `System.out.println` — those are ordinary Java classes whose bytecode must come from the JRE's bundled library.

### Core Components

**1. Core class libraries**
- Pre-Java 9: monolithic `rt.jar` (~60MB), loaded eagerly
- Java 9+ (Project Jigsaw / module system): split into modules — `java.base` (core: `java.lang`, `java.util`, `java.io`, `java.nio`), plus `java.sql`, `java.desktop`, `java.logging`, etc.
  ```
  java --list-modules
  ```

**2. Deployment/launcher**
- The **`java` launcher** — starts a JVM instance, loads your main class, invokes `main(String[] args)`

**3. UI toolkits**
- **AWT** — original GUI toolkit, native OS widgets
- **Swing** — pure-Java GUI, built on AWT
- **Java2D** — 2D graphics rendering

**4. Integration libraries**
- **JDBC** — database connectivity
- **JNDI** — naming/directory service lookups (e.g., LDAP)
- **RMI** — remote method invocation across JVMs/network

**5. Security & internationalization**
- **JCE** (cryptography), **JSSE** (SSL/TLS)
- i18n/l10n — locale formatting, resource bundles

**6. Supporting files**
- Property files, fonts/resources (AWT/Swing text rendering), timezone data

### Java 9+ Module System (`jlink`)
Pre-Java 9: even "Hello World" conceptually depended on the entire `rt.jar`. Post-Java 9:
- Libraries split into named modules with explicit dependencies (`module-info.java`)
- `jlink` builds a **custom minimal JRE** with only the modules your app needs — great for containers/microservices

```
jlink --module-path $JAVA_HOME/jmods --add-modules java.base,java.logging --output myCustomRuntime
```

### Concrete Example
```java
import java.util.ArrayList;
public class Demo {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("Hi");
        System.out.println(list);
    }
}
```
- JVM launches, loads `Demo.class` via Application ClassLoader
- On `new ArrayList<>()`, JVM needs `ArrayList.class`'s bytecode → resolved from `java.base` module (JRE), loaded via Bootstrap ClassLoader
- Without JRE's libraries: `NoClassDefFoundError: java/util/ArrayList` even though JVM itself runs fine — proves JVM ≠ JRE

### JRE Interview Q&A
- **Run `.class` with only JVM, no JRE?** → Practically no — nothing runs without `java.lang.Object`. Distinction is architectural, not literally separable.
- **Can JRE compile code?** → No, no `javac`.
- **Why did standalone JRE downloads stop after Java 11?** → Push toward modular model — build minimal custom runtimes via `jlink` instead.

---

## 5. JDK — Deep Dive

### What It Is
**JRE + full toolchain** for developing, compiling, packaging, debugging, monitoring Java applications. The only one of the three needed to *write* Java software.

### Core Components (Beyond JRE)

**1. Compiler**
- **`javac`** — converts `.java` → `.class` bytecode. Performs syntax/semantic checking, type checking, generics erasure, autoboxing insertion, produces bytecode + constant pool.

**2. Core dev tools**

| Tool | Purpose |
|---|---|
| `jar` | Bundles classes/resources into `.jar` (also `.war`/`.ear`) |
| `javadoc` | Generates HTML docs from `/** ... */` comments |
| `javap` | Disassembles `.class` into readable bytecode/signatures |
| `jdb` | CLI debugger — breakpoints, step-through, variable inspection |
| `jshell` | REPL (since Java 9) — instant snippet testing, no full compile |

**3. Diagnostic & monitoring tools**

| Tool | Purpose |
|---|---|
| `jps` | Lists running JVM processes |
| `jstack` | Dumps thread stack traces — debug deadlocks/hangs |
| `jmap` | Dumps heap memory — analyze leaks (`.hprof` for Eclipse MAT etc.) |
| `jstat` | Real-time GC and class-loading stats |
| `jconsole` / `jvisualvm` | GUI monitoring — CPU, heap, threads, GC |
| `jcmd` | Modern all-in-one diagnostic tool |

**4. Native development support**
- JNI header generation (`javac -h`, replaces deprecated `javah`)
- `src.zip` — standard library source, for IDE step-through debugging

**5. Build/module tools (Java 9+)**
- `jlink` — custom minimal runtime images
- `jmod` — packages modules needing native code (unlike plain `.jar`)
- `jdeps` — analyzes class/module dependencies — useful before modularizing legacy apps

### Interview Framing
> "If handed only a `.class` file, I need a JRE to run it. If I want to write, compile, debug, package, and monitor it in production, I need the full JDK."

### JDK Interview Q&A
- **Compile with only JRE?** → No, `javac` isn't in JRE.
- **`jar` vs `war` vs `ear`?** → `jar` = generic archive; `war` = Web Application Archive (servlet containers like Tomcat); `ear` = Enterprise Archive (bundles multiple wars/jars for Java EE)
- **Debug a hanging production JVM?** → `jstack` for thread dump (deadlocks/blocked threads), `jmap` heap dump if memory-related, `jstat`/GC logs for GC pressure
- **What does `javap -c MyClass.class` show?** → Disassembled bytecode per method — e.g., how a `switch` on Strings compiles to `hashCode` + `equals` checks

---

## 6. Extra-Layer Cheat Sheet (What Each Layer Adds)

| Layer | = Previous + | Exclusive Additions |
|---|---|---|
| **JVM** | (base) | ClassLoader, Runtime Data Areas, Execution Engine, JIT, GC |
| **JRE** | JVM + | `java.base` (java.lang, java.util, java.io...), AWT/Swing, JDBC, JNDI, RMI, JCE/JSSE, `java` launcher |
| **JDK** | JRE + | `javac`, `jar`, `javadoc`, `jdb`, `jshell`, `javap`, `jconsole`, `jstack`/`jmap`/`jps`/`jstat`/`jcmd`, JNI headers, `src.zip`, `jlink`, `jmod`, `jdeps` |

**Soundbite to tie it together:**
> "JVM executes bytecode but knows nothing about Java's standard classes. JRE wraps the JVM with those standard libraries so real programs can actually run. JDK wraps the JRE further with the compiler and dev tools so you can produce that bytecode in the first place. It's execution → runnable environment → full development kit."

---

## 7. Master Comparison Table

| | JVM | JRE | JDK |
|---|---|---|---|
| **Definition** | Spec + engine executing bytecode | JVM + standard libraries + launcher | JRE + dev/compile/debug tools |
| **Can compile?** | No | No | Yes (`javac`) |
| **Can run programs?** | Yes (needs libs in practice) | Yes | Yes |
| **Platform dependent?** | Implementation: yes; Spec: no | Yes (bundles native JVM) | Yes |
| **Key components** | ClassLoader, Runtime Data Areas, Execution Engine | `java.base`, AWT/Swing, JDBC, `java` launcher | `javac`, `jar`, `javadoc`, `jdb`, `jshell`, `jstack`, `jmap`, `jlink` |
| **Who uses it** | Abstract — used internally by JRE | End users running Java apps | Developers building Java apps |

---

## 8. Rapid-Fire Q&A Bank (Final Review Pass)

1. **Why is Java called platform-independent if JVM itself is platform-dependent?**
   Bytecode is universal; each OS needs its own JVM binary to translate that bytecode to native instructions.

2. **What is the parent delegation model and why does it matter?**
   Child ClassLoader defers to parent first. Prevents core class spoofing/security issues (e.g., fake `java.lang.String`).

3. **Difference between JIT and Interpreter?**
   Interpreter: line-by-line, no compile overhead, slow on repetition. JIT: compiles hot methods to native code once, reused — better throughput for long-running code.

4. **Where do objects live vs. where do method calls live?**
   Objects → Heap (shared, GC-managed). Method frames/local vars → Stack (per-thread, LIFO, no GC).

5. **PermGen vs Metaspace?**
   PermGen: fixed-size, part of JVM memory, pre-Java 8, prone to OOM. Metaspace: native OS memory, dynamically grows, Java 8+.

6. **What does the Bytecode Verifier check?**
   Structural validity, access rules, no illegal type casts, no stack overflow/underflow — a security boundary before execution.

7. **What's the role of the `java` launcher?**
   Starts a JVM instance, loads the main class, invokes `main(String[])` — part of JRE.

8. **What is `jlink` and why does it matter?**
   Java 9+ tool that builds a custom minimal runtime image with only required modules — smaller footprint for containers/microservices.

9. **Difference between `jar`, `war`, `ear`?**
   `jar`: generic archive. `war`: web app archive for servlet containers. `ear`: enterprise archive bundling multiple wars/jars.

10. **How to debug a hanging production JVM?**
    `jstack` for thread/deadlock analysis, `jmap` for heap dump on memory issues, `jstat`/GC logs for GC pressure diagnosis.

---

*End of revision notes. Good luck with the interview!*
