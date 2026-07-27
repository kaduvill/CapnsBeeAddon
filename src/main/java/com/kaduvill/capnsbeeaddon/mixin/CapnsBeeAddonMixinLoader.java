package com.kaduvill.capnsbeeaddon.mixin;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

public final class CapnsBeeAddonMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(
                "capnsbeeaddon.mixins.json"
        );
    }
}
