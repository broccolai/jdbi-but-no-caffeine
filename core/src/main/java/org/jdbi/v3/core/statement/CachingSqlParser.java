/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.core.statement;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import org.jdbi.v3.meta.Beta;

abstract class CachingSqlParser implements SqlParser {
    private final LoadingCache<String, ParsedSql> parsedSqlCache;

    CachingSqlParser() {
        this(CacheBuilder.newBuilder()
                     .maximumSize(1_000));
    }

    CachingSqlParser(CacheBuilder<Object, Object> cache) {
        parsedSqlCache = cache.build(new Loader());
    }

    @Override
    public ParsedSql parse(String sql, StatementContext ctx) {
        try {
            return parsedSqlCache.get(sql);
        } catch (IllegalArgumentException | ExecutionException e) {
            throw new UnableToCreateStatementException("Exception parsing for named parameter replacement", e, ctx);
        }
    }

    @Beta
    public CacheStats cacheStats() {
        return parsedSqlCache.stats();
    }

    abstract ParsedSql internalParse(String sql);

    class Loader extends CacheLoader<String, ParsedSql> {

        @Override
        public ParsedSql load(String o) throws Exception {
            return internalParse(o);
        }
    }
}
