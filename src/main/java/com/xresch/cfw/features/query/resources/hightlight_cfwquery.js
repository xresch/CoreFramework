/*
Language: Bash
Author: vah <vahtenberg@gmail.com>
Contributrors: Benjamin Pannell <contact@sierrasoftworks.com>
Website: https://www.gnu.org/software/bash/
Category: common
*/

/** @type LanguageFn */
function cfw_hljs_register_CFWQuery(hljs) {
  const regex = hljs.regex;
  const VAR = {};
  const BRACED_VAR = {
    begin: /\$\{/,
    end:/\}/,
    contains: [
      "self",
      {
        begin: /:-/,
        contains: [ VAR ]
      } // default values
    ]
  };
	
  const COMMENTS = {
	scope: 'comment',
	begin: /\|\s*?(comment|off)+/,
	end: /[^\|]*/	
	
  }
  Object.assign(VAR,{
    scope: 'variable',
    variants: [
      {begin: regex.concat(/\$[\w\d#@][\w\d_]*/,
        // negative look-ahead tries to avoid matching patterns that are not
        // Perl at all like $ident$, @ident@, etc.
        `(?![\\w\\d])(?![$])`) },
      BRACED_VAR
    ]
  });

  const SUBST = {
    scope: 'subst',
    begin: /\$\(/, end: /\)/,
    contains: [hljs.BACKSLASH_ESCAPE]
  };

  const HERE_DOC = {
    begin: /<<-?\s*(?=\w+)/,
    starts: {
      contains: [
        hljs.END_SAME_AS_BEGIN({
          begin: /(\w+)/,
          end: /(\w+)/,
          scope: 'string'
        })
      ]
    }
  };

  const QUOTE_STRING = {
    scope: 'string',
    begin: /"/, end: /"/,
    contains: [
      hljs.BACKSLASH_ESCAPE,
      VAR,
      SUBST
    ]
  };
  SUBST.contains.push(QUOTE_STRING);

  const ESCAPED_QUOTE = {
    scope: '',
    begin: /\\"/

  };
  const APOS_STRING = {
    scope: 'string',
    begin: /'/, end: /'/,
	contains: [
      hljs.BACKSLASH_ESCAPE,
      VAR,
      SUBST
    ]
  };

  const ARITHMETIC = {
    begin: /\$\(\(/,
    end: /\)\)/,
    contains: [
      { begin: /\d+#[0-9a-f]+/, scope: "number" },
      hljs.NUMBER_MODE,
      VAR
    ]
  };

  const FUNCTION = {
    scope: 'function',
    begin: /\w[\w\d_]*\s*\(\s*\)\s*\{/,
    returnBegin: true,
    contains: [hljs.inherit(hljs.TITLE_MODE, {begin: /\w[\w\d_]*/})],
    relevance: 0
  };

  const KEYWORDS = [
    "AND",
    "OR",
    "NOT",
  ];

  const LITERALS = [
    "true",
    "false",
	"null"
  ];

  // to consume paths to prevent keyword matches inside them
  const PATH_MODE = {
    match: /(\/[a-z._-]+)+/
  };

  const NUMBERS = {
    scope: 'number',
    begin: /-?\d/,
	end: /[\d\.]*/,
  }

  const SOURCE = {
    scope: 'built_in',
    begin: /source\s+/,
	end: /[^\s]*/,
	}

  const COMMAND_NAMES = [
    "comment",
	"dedup",
	"display",
	"distinct",
	"fieldformat",
	"first",
	"formatfield",
	"keep",
	"last",
	"meta",
	"metadata",
	"off",
	"remove",
	"rename",
	"reorder",
	"src",
	"tail",
	"top",
	"uniq",
  ];

  return {
    name: 'cfwquery',
    aliases: ['cfwql'],
    keywords: {
      $pattern: /\b[a-z._-]+\b/,
      keyword: [
		...COMMAND_NAMES,
        
        ...KEYWORDS,
      ],
      literal: LITERALS,
      built_in:[],
    },
    contains: [
	  SOURCE,
	  NUMBERS,
      FUNCTION,
      ARITHMETIC,
	  COMMENTS,
      hljs.HASH_COMMENT_MODE,
      HERE_DOC,
      PATH_MODE,
      QUOTE_STRING,
      ESCAPED_QUOTE,
      APOS_STRING,
      VAR
    ]
  };
}

hljs.registerLanguage("cfwquery", cfw_hljs_register_CFWQuery);