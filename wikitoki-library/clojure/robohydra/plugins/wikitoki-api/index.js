var heads               = require("robohydra").heads,
    RoboHydraHead       = heads.RoboHydraHead,
    RoboHydraHeadStatic = heads.RoboHydraHeadStatic;

exports.getBodyParts = function(conf, modules) {
    "use strict";

    var assert = modules.assert;
    return {
        heads: [
            new RoboHydraHead({
                path: '/api/pages/:name',
                handler: function(req, res) {
                    var newState = JSON.parse(req.rawBody);

                    res.send(JSON.stringify({
                        name: req.params.name,
                        contents: newState.contents
                    }));
                }
            })
        ]
    };
};
