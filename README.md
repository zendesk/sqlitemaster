SQLiteMaster
============
Android library for getting existing db schema information from sqlite_master table.

Basic usage
-----------
You can use the schema information in your SQLiteOpenHelper's `onCreate` and `onUpgrade` to remove some boilerplate code. Compare:

```java
db.execSQL("DROP TRIGGER IF EXISTS trigger_a");
db.execSQL("DROP TRIGGER IF EXISTS trigger_b");
db.execSQL("DROP TRIGGER IF EXISTS trigger_c");
// ...
db.execSQL("DROP TRIGGER IF EXISTS trigger_z");
```

With:
```java
SQLiteMaster.dropTriggers(db);
```

You can perform similar operations with views, tables and indexes, or you can access the full schema information using `getSQLiteSchemaParts(SQLiteDatabase db, SQLiteSchemaPartType partType)` or `getSQLiteSchemaParts(SQLiteDatabase db)`, which return the list of `SQLiteSchemaPart` objects:

```java
public class SQLiteSchemaPart {
  public final String name;
  public final String sql;
  public final String type;
}
```

What you do with that information is completely up to you.

Building
--------
This is standard maven project. To build it just execute:
```shell
mvn clean package
```
in directory with pom.xml.

Is it safe to use?
------------------
Our tests indicate that there are no issues whatsoever on API level 8+ (Android 2.2). We haven't tested earlier versions, so consider yourself warned (and please let us know if you confirm it works on lower API levels!).

Todo
----
* Documentation
* Unit tests

License
-------
    Copyright (C) 2013 Jerzy Chalupski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
