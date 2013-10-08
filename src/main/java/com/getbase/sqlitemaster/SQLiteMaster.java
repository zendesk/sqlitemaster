/*
 * Copyright (C) 2013 Jerzy Chalupski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.getbase.sqlitemaster;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

import java.util.List;

public final class SQLiteMaster {
  private static final CursorFactory CURSOR_FACTORY = new CursorFactory() {
    @SuppressWarnings("deprecation")
    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
      return new SQLiteCursor(db, masterQuery, editTable, query);
    }
  };

  private SQLiteMaster() {
  }

  private interface SqliteMasterColumns {
    public static final String NAME = "name";
    public static final String SQL = "sql";
    public static final String TYPE = "type";
  }

  private static final String SQLITE_MASTER_TABLE = "sqlite_master";

  public static List<SQLiteSchemaPart> getSQLiteSchemaParts(SQLiteDatabase db, SQLiteSchemaPartType partType) {
    return getSQLiteSchemaParts(
        db.queryWithFactory(
            CURSOR_FACTORY,
            false,
            SQLITE_MASTER_TABLE,
            null,
            SqliteMasterColumns.TYPE + "= ?",
            new String[] { partType.getTypeName() },
            null,
            null,
            SqliteMasterColumns.NAME,
            null
        )
    );
  }

  public static List<SQLiteSchemaPart> getSQLiteSchemaParts(SQLiteDatabase db) {
    return getSQLiteSchemaParts(
        db.queryWithFactory(
            CURSOR_FACTORY,
            false,
            SQLITE_MASTER_TABLE,
            null,
            null,
            null,
            null,
            null,
            SqliteMasterColumns.NAME,
            null
        )
    );
  }

  private static List<SQLiteSchemaPart> getSQLiteSchemaParts(Cursor c) {
    List<SQLiteSchemaPart> result = Lists.newArrayList();
    if (c != null) {
      try {
        if (c.moveToFirst()) {
          do {
            result.add(new SQLiteSchemaPart(
                c.getString(c.getColumnIndexOrThrow(SqliteMasterColumns.NAME)),
                c.getString(c.getColumnIndexOrThrow(SqliteMasterColumns.SQL)),
                c.getString(c.getColumnIndexOrThrow(SqliteMasterColumns.TYPE))
            ));
          } while (c.moveToNext());
        }
      } finally {
        c.close();
      }
    }
    return result;
  }

  private static final Function<SQLiteSchemaPart, String> GET_SCHEMA_PART_NAME = new Function<SQLiteSchemaPart, String>() {
    @Override
    public String apply(SQLiteSchemaPart input) {
      return input.name;
    }
  };

  public static void dropTriggers(SQLiteDatabase db) {
    Iterable<String> triggerNames = FluentIterable
        .from(getSQLiteSchemaParts(db, SQLiteSchemaPartType.TRIGGER))
        .transform(GET_SCHEMA_PART_NAME);

    for (String triggerName : triggerNames) {
      db.execSQL("DROP TRIGGER IF EXISTS " + triggerName);
    }
  }

  private static final Predicate<SQLiteSchemaPart> IS_AUTO_INDEX = new Predicate<SQLiteSchemaPart>() {
    @Override
    public boolean apply(SQLiteSchemaPart input) {
      return input.name.startsWith("sqlite_");
    }
  };

  public static void dropIndexes(SQLiteDatabase db) {
    Iterable<String> indexNames = FluentIterable
        .from(getSQLiteSchemaParts(db, SQLiteSchemaPartType.INDEX))
        .filter(Predicates.not(IS_AUTO_INDEX))
        .transform(GET_SCHEMA_PART_NAME);

    for (String indexName : indexNames) {
      db.execSQL("DROP INDEX IF EXISTS " + indexName);
    }
  }

  public static void dropViews(SQLiteDatabase db) {
    Iterable<String> viewsNames = FluentIterable
        .from(getSQLiteSchemaParts(db, SQLiteSchemaPartType.VIEW))
        .transform(GET_SCHEMA_PART_NAME);

    for (String viewName : viewsNames) {
      db.execSQL("drop view if exists " + viewName);
    }
  }
}
