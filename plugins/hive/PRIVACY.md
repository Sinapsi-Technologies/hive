# Privacy

The Hive plugin connects by default only to a Hive MCP server operated by the
user on `http://127.0.0.1:9090/mcp`. The plugin package does not include
credentials and does not declare a third-party MCP endpoint.

Project paths, source information, domain models, dependency data, and tool
arguments requested by the agent are exchanged with that local server so it can
perform the selected operation. Their handling and retention depend on the Hive
MCP instance operated by the user.

The server can access external dependency and security providers when those
features are enabled in its configuration. This can include Maven repositories,
Sonatype Guide, and OSV tooling. The server operator controls those settings,
credentials, and the applicable provider policies.

If the MCP endpoint is changed to a remotely deployed instance, the operator of
that instance is responsible for documenting its data handling, access controls,
retention, and applicable privacy policy.
