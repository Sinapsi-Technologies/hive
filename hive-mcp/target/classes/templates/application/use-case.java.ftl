package ${packageName};

import io.sinapsi.hive.core.result.Result;
import io.sinapsi.hive.core.result.Unit;
import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.core.usecase.UseCase;
${commandImportsBlock!}
<#if factory>
import io.sinapsi.hive.factory.AbstractCommandFactory;
</#if>

public interface ${className}UseCase extends UseCase<${className}UseCase.${commandType}, Result<Unit>> {

    record ${commandType}(
<#list commandFields as field>
        ${field.type} ${field.name}<#sep>,</#sep>
</#list>
    ) implements Command {
    }

<#if factory>
    final class CommandFactory extends AbstractCommandFactory<${commandType}> {

        private CommandFactory() {
        }

        public static Result<${commandType}> create(${commandParameters}) {
            return new CommandFactory().validate(new ${commandType}(${commandArguments}));
        }
    }
</#if>
}
