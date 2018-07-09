package org.talend.components.simplefileio.runtime;

public interface SchemaConstant {

    /**
     * we pass this to DatasetRuntime.getSample interface to 
     * tell the getSample method we only fetch the schema information,
     * then expect getSample method return one row which contains schema only.
     * we do this as file is possible only have header for schema, but no data, that is valid,
     * so can't pass "1" to getSample, if pass "1", it will return 0 row which is expected.
     * 
     * in fact, getSchema and getSample method should call different code, but for file, for code reuse,
     * getSchema call getSample method, this is the reason of current constant.
     */
    int ONLY_FETCH_SCHEMA_SIGN = -3;
}
