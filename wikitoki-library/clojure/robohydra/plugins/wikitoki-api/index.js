var heads               = require("robohydra").heads,
    RoboHydraHeadStatic = heads.RoboHydraHeadStatic;

exports.getBodyParts = function() {
    return {
        scenarios: {
            twoPages: {
                heads: [
                    new RoboHydraHeadStatic({
                        path: '/api/pages',
                        content: {
                            pages: {
                                WikiIndex: "This is the index from RoboHydra.",
                                RoboHydra: "Cool testing tool."
                            }
                        }
                    })
                ]
            }
        }
    };
};
