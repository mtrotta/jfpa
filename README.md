# Java File Persistence Api
Welcome to JFPA - Java File Persistence Api

JFPA is a small library that helps you reading and writing text files containing data.
It can handle positional or delimited records, single line or multiple line records.
Configuration is made by annotation or by class inheritance.

## Basic examples with annotations

### Defining a record mapping

#### Delimited

Delimited columns with positions mapped by header

```java
@Delimited
public static class ExampleDelimitedHeader {
    @TextColumn(name = "COL1")
    private String value1;
    @TextColumn(name = "COL2")
    private String value2;
    @TextColumn(name = "COL3")
    private String value3;

    // setters and getters
}
```

Delimited columns separated by ';' with order specified by class fields

```java
@Delimited
public class ExampleDelimited {
    @TextColumn
    private String f1;
    @TextColumn(offset = 1)
    private String f4;
    @TextColumn
    private Date date;
    @TextColumn(dateFormat = "yyyy/MM/dd")
    private Date dateFormatted;
    @TextColumn
    private Long longVal;
    @TextColumn
    private Double doubleVal;
    @TextColumn
    private Boolean bool;
    @TextColumn(booleanFormat = {"T","F"})
    private Boolean boolFormatted;
    @TextColumn
    private String last;
    
    // setters and getters
}
```

#### Positional

Positional columns with fixed length

```java
@Positional
public static class ExamplePositional {
    @TextColumn(length = 3)
    private String value1;
    @TextColumn(length = 10)
    private String value2;
    @TextColumn(length = 15)
    private String value3;

    // setters and getters
}
```

### Reading from a text file

```java
public class Test {
    public static void main(String[] args){
        RecordManager manager = new RecordManager();
        String line = "1;2;3"; // Usually line is read from a text file
        ExampleDelimited record = manager.read(line, ExampleDelimited.class);
    }
}
```
### Writing to a text file

```java
public class Test {
    public static void main(String[] args){
        RecordManager manager = new RecordManager();
        ExampleDelimited record = new ExampleDelimited();
        String line = manager.write(record);
    }
}
```

This was just a brief introduction, but with this library you can also read and write multi-line records and binary streams.

---
Copyright 2011-2012 Matteo Trotta