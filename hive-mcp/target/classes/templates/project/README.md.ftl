# ${projectName}

Generated with hive-toolkit.

Application output records named `*View` belong in `application.ports.in.views`
and expose a static `of(...)` factory. Use cases can return `Result<MyView>`;
use `Result<Unit>` for operations without output data. Input ports and views
must not depend on `application.services`.
