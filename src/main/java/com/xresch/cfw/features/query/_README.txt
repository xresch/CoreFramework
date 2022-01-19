#################################################################
# Components
#################################################################
- Tokenizer: Responsible to create the tokens from the query string.
- Parser: Takes the tokens and converts them into Queries, Commands and QueryParts
- QueryPart: Simplification Layer for tokens, Query parts are passed to Sources, Commands and Functions,
  which then can do their job based on the data provided by the tokens.
- Sources: A source of data that will be used to read the data and convert it to EnhancedJsonObject that are passed through the pipeline.
    - Sources are registered by calling CFW.Registry.Query.registerSource().
- Commands: The basic commands that can be used in a query to read and process data.
- Function: Can be called by commands to process data.
- EnhancedJsonObject: A wrapper for GSON JsonObject adding additional methods.

#################################################################
# Basic workflow
#################################################################
- CFWQueryExecutor is the main interface to execute queries
  - Calls CFWQueryParser
    - First calls CFWTokenizer and creates all tokens
    - Then converts tokens into QueryParts
  - Takes the resulting queries and executes each query
    - Queries are executed serial
    - The commands of the queries are piped and executed in parallel.
  - Reads the results and converts them to a result format