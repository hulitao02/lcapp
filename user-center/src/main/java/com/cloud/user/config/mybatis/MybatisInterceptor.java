package com.cloud.user.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import java.util.Properties;

@Component
@Intercepts({
        @org.apache.ibatis.plugin.Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @org.apache.ibatis.plugin.Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@Slf4j
public class MybatisInterceptor implements Interceptor {


    private static final int MAPPED_STATEMENT_INDEX = 0;
    private static final int PARAM_OBJ_INDEX = 1;

    private class BoundSqlSource implements SqlSource {
        private BoundSql boundSql;
        private BoundSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
    /*自定义SQL*/
    private String resetSql(String sql) {
        if (sql.indexOf("app_user") > -1 && sql.indexOf("level") > -1) {
            sql = sql.replace("level", " \"level\" ");
        }
        return sql;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        resetKingBaseSql(invocation);

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private void resetKingBaseSql(Invocation invocation) {
        final Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        ParameterMap map = mappedStatement.getParameterMap();
        BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
        String sql = boundSql.getSql();

//       目前 只看到了app_user中，有level属性，数据库关键字 。修改 。
        if(sql.indexOf("app_user") > -1 && sql.indexOf("level") > -1){
            BoundSqlSource boundSqlSource = new BoundSqlSource(boundSql);
            MappedStatement newMappedStatement = copyFromMappedStatement(mappedStatement, boundSqlSource,map);
            MetaObject metaObject = MetaObject.forObject(newMappedStatement,
                    new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
                    new DefaultReflectorFactory());
            //log.info("修改前SQL: {}",sql);
            String newSql = resetSql(boundSql.getSql());
            metaObject.setValue("sqlSource.boundSql.sql", newSql);
            //log.info(" 修改后SQL: {}",newSql);
            args[MAPPED_STATEMENT_INDEX] = newMappedStatement;
        }
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource,
                                                    ParameterMap parameterMap) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(),
                newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        // builder.keyProperty(ms.getKeyProperty());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(parameterMap);
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

}