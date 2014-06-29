var RoboHydraHeadStatic = require("robohydra").heads.RoboHydraHeadStatic;

exports.getBodyParts = function() {
    return {
        heads: [
            new RoboHydraHeadStatic({
                path: '/api/pages',
                content: {
                    pages: {
                        WikiIndex: "This is the index from RoboHydra."
                    }
                }
            })
        ]
    };
};
