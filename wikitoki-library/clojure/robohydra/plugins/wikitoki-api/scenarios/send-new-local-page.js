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
                        WikiIndex: "First page"
                    }
                }
            }),

            new RoboHydraHead({
                path: '/api/pages/NewLocalPage',
                handler: function(req, res) {
                    assert.ok(req.method === "POST",
                              "Request should be POST");
                    var newState = JSON.parse(req.rawBody);
                    var contents = newState.contents;
                    assert.equal(contents, "New page!",
                                 "Page contents should be correct");

                    res.send(JSON.stringify({
                        name: "NewLocalPage",
                        contents: contents
                    }));
                }
            })
        ]
    };
};
