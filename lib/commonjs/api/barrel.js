"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _StreamLayerView = require("./StreamLayerView");
Object.keys(_StreamLayerView).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _StreamLayerView[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _StreamLayerView[key];
    }
  });
});
var _StreamLayer = require("./StreamLayer");
Object.keys(_StreamLayer).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _StreamLayer[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function () {
      return _StreamLayer[key];
    }
  });
});
//# sourceMappingURL=barrel.js.map