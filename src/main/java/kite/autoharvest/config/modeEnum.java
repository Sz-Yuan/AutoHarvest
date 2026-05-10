package kite.autoharvest.config;

import kite.autoharvest.mode.*;

public enum ModeEnum {
    weed {
        @Override
        public AutoMode setMode() {
            return new WeedMode();
        }
    },
    plant {
        @Override
        public AutoMode setMode() {
            return new PlantMode();
        }
    },
    bonemeal {
        @Override
        public AutoMode setMode() {
            return new BonemealMode();
        }
    },
    harvest {
        @Override
        public AutoMode setMode() {
            return new HarvestMode();
        }
    },
    hoe {
        @Override
        public AutoMode setMode() {
            return new HoeMode();
        }
    },

    farmer {
        @Override
        public AutoMode setMode() {
            return CompositeMode.farmer();
        }
    },

    feed {
        @Override
        public AutoMode setMode() {
            return new FeedMode();
        }
    },

    fishing {
        @Override
        public AutoMode setMode() {
            return new FishingMode();
        }
    };

    public abstract AutoMode setMode();
}