Java 11 brought several significant changes and enhancements compared to Java 8, building on the features introduced in Java 9 and 10. Here's a detailed overview of the key changes, along with examples:

### 1. **Long-Term Support (LTS)**

Java 11 is a Long-Term Support release, meaning it will receive updates and support for a longer period compared to non-LTS versions.

### 2. **Removal of Java EE and CORBA Modules**

Java 11 removed several modules that were deprecated in earlier versions, including Java EE and CORBA modules.

### 3. **New `HttpClient` API**

Java 11 introduced a new `HttpClient` API to replace the old `HttpURLConnection`, providing a more modern and flexible way to handle HTTP requests.

**Example:**

```java

import java.net.http.HttpClient;

import java.net.http.HttpRequest;

import java.net.http.HttpResponse;

HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()

    .uri(URI.create("https://api.example.com/data"))

    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println(response.body());

```

### 4. **String Methods**

Java 11 added several new methods to the `String` class, including `isBlank()`, `lines()`, `strip()`, and `repeat(int)`.

**Example:**

```java

String text = " Hello, World! ";

System.out.println(text.isBlank()); // false

System.out.println(text.strip()); // "Hello, World!"

System.out.println("abc".repeat(3)); // "abcabcabc"

```

### 5. **Local-Variable Syntax for Lambda Parameters**

Java 11 allows the use of `var` for lambda parameters, improving type inference.

**Example:**

```java

List<String> list = List.of("Java", "Python", "JavaScript");

list.forEach((var item) -> System.out.println(item));

```

### 6. **New `Files` Methods**

New utility methods were added to the `Files` class for easier file handling.

**Example:**

```java

Path path = Paths.get("example.txt");

Files.writeString(path, "Hello, World!", StandardOpenOption.CREATE);

String content = Files.readString(path);

System.out.println(content);

```

### 7. **Optional Enhancements**

Java 11 introduced the `Optional` class methods `isEmpty()`.

**Example:**

```java

Optional<String> optionalValue = Optional.ofNullable(null);

System.out.println(optionalValue.isEmpty()); // true

```

### 8. **Pattern Matching for `instanceof` (Preview Feature)**

Java 11 included a preview feature for pattern matching with `instanceof`, which simplifies type checks and casts.

**Example:**

```java

Object obj = "Hello";

if (obj instanceof String s) {

    System.out.println(s.toUpperCase()); // "HELLO"

}

```

### 9. **Dynamic Class-File Constants**

Java 11 introduced a new constant pool that allows dynamic constants to be used in class files, improving efficiency.

### 10. **Removal of Applet API**

The Applet API was removed as part of the modernization of Java, reflecting the decline in use of applets on the web.

### 11. **Enhanced `Garbage Collection`**

Java 11 included enhancements to garbage collection, particularly to the G1 collector, for better performance.

### 12. **New Standard Libraries and API Updates**

- New classes and methods added to the `java.util` and `java.nio` packages for various enhancements.

- The `java.security` package saw improvements in security and cryptographic standards.

### Summary

Java 11 introduced numerous enhancements aimed at modernizing the language and improving developer productivity. Key features like the new `HttpClient`, string methods, and local-variable syntax for lambda parameters significantly enhance the usability of Java.

