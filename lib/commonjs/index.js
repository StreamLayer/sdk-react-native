"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _exportNames = {
  StreamLayerView: true
};
Object.defineProperty(exports, "StreamLayerView", {
  enumerable: true,
  get: function () {
    return _StreamLayerView.StreamLayerView;
  }
});
var _barrel = require("./api/barrel");
Object.keys(_barrel).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (Object.prototype.hasOwnProperty.call(_exportNames, key)) return;
  if (key in exports && exports[key] === _barrel[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _barrel[key];
    }
  });
});
var _StreamLayerView = require("./internal/StreamLayerView");
//# sourceMappingURL=index.js.map