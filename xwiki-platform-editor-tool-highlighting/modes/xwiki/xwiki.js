CodeMirror.defineMode("xwiki", function(config, parserConfig) {
  var xwikitagsMode = CodeMirror.getMode(config, "xwikitags");
  var htmlMode      = CodeMirror.getMode(config, {name: "xml", htmlMode: true});
  var jsMode        = CodeMirror.getMode(config, "javascript");
  var velocityMode  = CodeMirror.getMode(config, "velocity");
  var cssMode       = CodeMirror.getMode(config, "css");

  function html(stream, state) {
    var style = xwikitagsMode.token(stream, state.htmlState);
    if (style == "tag" && stream.current() == "}}" && state.htmlState.context) {
      if (/^script$/i.test(state.htmlState.context.tagName)) {
        state.token = javascript;
        state.localState = jsMode.startState(xwikitagsMode.indent(state.htmlState, ""));
        state.mode = "javascript";
      }
      else if (/^style$/i.test(state.htmlState.context.tagName)) {
        state.token = css;
        state.localState = cssMode.startState(xwikitagsMode.indent(state.htmlState, ""));
        state.mode = "css";
      } else if(/^velocity$/i.test( state.htmlState.context.tagName )){
        state.token = velocity;
        state.localState = velocityMode.startState(xwikitagsMode.indent(true, ""));
        state.mode = "velocity";
      } 
    }
    return style;
  }
  function maybeBackup(stream, pat, style) {
    var cur = stream.current();
    var close = cur.search(pat);
    if (close > -1) stream.backUp(cur.length - close);
    return style;
  }
  function velocity(stream, state) {
    if (stream.match(/^\{\{\/\s*velocity\s*\}\}/i, false)) {
      state.token = html;
      state.curState = null;
      state.mode = "html";
      return html(stream, state);
    }
    return maybeBackup(stream, /\{\{\/\s*velocity\s*\}\}/, velocityMode.token(stream, state.localState));
  }
  function javascript(stream, state) {
    if (stream.match(/^<\/\s*script\s*>/i, false)) {
      state.token = html;
      state.curState = null;
      state.mode = "html";
      return html(stream, state);
    }
    return maybeBackup(stream, /<\/\s*script\s*>/,
                       jsMode.token(stream, state.localState));
  }
  function css(stream, state) {
    if (stream.match(/^<\/\s*style\s*>/i, false)) {
      state.token = html;
      state.localState = null;
      state.mode = "html";
      return html(stream, state);
    }
    return maybeBackup(stream, /<\/\s*style\s*>/,
                       cssMode.token(stream, state.localState));
  }

  return {
    startState: function() {
      var state = xwikitagsMode.startState();
      return {token: html, localState: null, mode: "html", htmlState: state};
    },

    copyState: function(state) {
      if (state.localState)
        var local = CodeMirror.copyState(state.token == css ? cssMode : jsMode, state.localState);
      return {token: state.token, localState: local, mode: state.mode,
              htmlState: CodeMirror.copyState(htmlMode, state.htmlState)};
    },

    token: function(stream, state) {
      return state.token(stream, state);
    },

    indent: function(state, textAfter) {
      if (state.token == html || /^\s*<\//.test(textAfter))
        return htmlMode.indent(state.htmlState, textAfter);
      else if (state.token == javascript)
        return jsMode.indent(state.localState, textAfter);
      else
        return cssMode.indent(state.localState, textAfter);
    },

    electricChars: "/{}:"
  }
});

CodeMirror.defineMIME("text/html", "htmlmixed");
