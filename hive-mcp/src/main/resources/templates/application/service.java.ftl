package ${packageName};

import ${inputPortPackage}.${useCaseType};
import io.sinapsi.hive.core.result.Result;
import io.sinapsi.hive.core.result.Unit;

public final class ${className} implements ${useCaseType} {

    @Override
    public ${returnType} handle(${useCaseType}.${commandType} command) {
        ${body}
    }
}
