var heads               = require("robohydra").heads,
    RoboHydraHead       = heads.RoboHydraHead,
    RoboHydraHeadStatic = heads.RoboHydraHeadStatic;

exports.getBodyParts = function(_, modules) {
    var assert = modules.assert;
    return {
        heads: [
            new RoboHydraHeadStatic({
                path: '/api/pages',
                content: {
                    pages: {
                        WikiIndex: "This is the index from RoboHydra.",
                        RoboHydra: "Cool testing tool."
                    }
                }
            }),

            new RoboHydraHead({
                path: '/api/pages/WikiIndex',
                handler: function(req, res) {
                    assert.ok((req.method === "POST" ||
                               req.method === "PUT"),
                              "Request should be POST or PUT");
                    var newState = JSON.parse(req.rawBody);
                    var contents = newState.contents;
                    assert.equal(contents,
                                 "This is the index from RoboHydra." +
                                 "\nThis is a second line",
                                 "Page contents should be correct");

                    res.send(JSON.stringify({
                        name: "WikiIndex",
                        contents: contents
                    }));
                }
            })
        ]
    };
};
