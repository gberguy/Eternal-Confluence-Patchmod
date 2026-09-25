package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import static com.gberguy.ecpatches.core.PatchId.*;

public final class GhostlyPatches {
    private GhostlyPatches() {
    }

    public static MethodPatch[] create() {
        return new MethodPatch[]{
            new GhostlyTargetPatch("net.minecraft.entity.ai.EntityAITarget", "func_179445_a", "(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/entity/EntityLivingBase;ZZ)Z",
                    new String[]{"c113d04b382bb25e3f28c182d5b65b7b54d2ee8b86420a50d978235cc3c079c5", "eac00dfbec43b1a2ae5996ea51eb392ed2f8afe275500da35c87d6287ebb827e"},
                    new String[]{"14de443983ee462c6a81f5e2156f9f3b8dc78a10f022943bccb601451c42a5ef", "2d54ee595818746d008037ae77af1ba1816b83f827ea63d244f6caa75913d79c"},
                    null, null, LYCANITES_GHOST, WIZARDRY_GHOST, ANCIENT_GHOST, TORO_GHOST),
            new GhostlyTargetPatch("electroblob.wizardry.entity.living.EntityEvilWizard", "lambda$initEntityAI$0", "(Lnet/minecraft/entity/Entity;)Z",
                    new String[]{"2d139c49e145f5f913fced380aa7781b50de969b705add955fceafefef349baa", "98a9016ea2d8ec5b1e7a3e67723edf048b0e924477a56ab8c3543b41e23aa254"},
                    new String[]{"12542441e85f2ff1adbf4c1575516da7d5270723fe970d405b4d2c5d194646d0", "3c9f0224e27a1cc0a797c52241f943e28d19fcb584dbbbae9595709955ff3406"},
                    null, null, WIZARDRY_GHOST, ANCIENT_GHOST),
            new GhostlyTargetPatch("electroblob.wizardry.entity.living.ISummonedCreature", "isValidTarget", "(Lnet/minecraft/entity/Entity;)Z",
                    new String[]{"353f38f9944eca2ce95a20dc98a8c45972b4cb785ba7116bb4b58f74f780ee9a", "58fef7a40bf10e49334c00095f696eef2ee2a4447f69d0c2578367b753ecb74d"},
                    new String[]{"099b706fe5df571abc88b2d37e92486358b2b111b90e0c8454d2a31bc03b7ca1", "d284ea9c540287fc39cd9e7aec6a047689fa337c4e2dc9eba4930d06eeb48032"},
                    null, null, WIZARDRY_GHOST, ANCIENT_GHOST),
            new GhostlyTargetPatch("electroblob.wizardry.entity.living.ISummonedCreature", "lambda$getTargetSelector$0", "(Lnet/minecraft/entity/Entity;)Z",
                    new String[]{"25a1120c1a11040c08226477b619c824ce44452945d05c73e76a7d9d5d6aaef9", "512226b2a22d01edca5f8b3a87040f2f3be2725fa0452bff83ba13d76c6e17f3"},
                    new String[]{"a1ea249a75024e7aad1e1013e60e2c51a9327dbf5a03598809d6c27dae34d0cf", "b4f3abfa6a10e92a3fd5a806bbb0494e39bd70514307ab218c74312c425107c7"},
                    null, null, WIZARDRY_GHOST, ANCIENT_GHOST),
            new GhostlyTargetPatch("net.torocraft.toroquest.entities.ai.EntityAIBanditAttack", "shouldAttackPlayer", "(Lnet/minecraft/entity/player/EntityPlayer;)Z",
                    new String[]{"dfcd66e80171130a33421ba332cf181dc062743c27093b2b6ff88b882cbb006d", "9e4a2a1ff9aad658cc8ee41d619457975e3913970ee95e7f967faf5e64219493"},
                    new String[]{"22801fa6a4bccccf6e31e69fd81b6ada32bdc218959dfb2dff3b4e349c40276a", "2f54d2f7b5a90ae4c57234a33f5f9652bbed456ae3f7a7c164002281baf8e98e"},
                    "field_75299_d", "net/torocraft/toroquest/entities/EntityToroMob", TORO_GHOST),
            new GhostlyTargetPatch("net.torocraft.toroquest.entities.ai.EntityAINearestAttackableCivTarget", "shouldAttackPlayerBasedOnCivilization", "(Lnet/minecraft/entity/player/EntityPlayer;)Z",
                    new String[]{"456fadc633ed30f4c6c8eef69ef5a6acee9ffe13c266420811ad92a70949d493", "dfa77cb49dda6a713455cb4ac6305e6d642c91261108c18c02f028025c365e60"},
                    new String[]{"e68a96f4491dd2d8611facad16760cee3a7984a0c3fc8405c567726041fa43ca", "e66b91cb37b8acd7f320dd45280c6e90576c3efa038aca3f69422ba7a1ce39a6"},
                    "field_75299_d", "net/torocraft/toroquest/entities/EntityGuard", TORO_GHOST),
            new GhostlyTargetPatch("net.torocraft.toroquest.entities.EntityMonolithEye$2", "apply", "(Lnet/minecraft/entity/player/EntityPlayer;)Z",
                    new String[]{"78606972ddcd88df5c186e5e58639a132a30e7bfc1be4b391dcffa166ea75f0f", "78606972ddcd88df5c186e5e58639a132a30e7bfc1be4b391dcffa166ea75f0f"},
                    new String[]{"05905136bd908ba4b31a4658cff1182386a1dcb600c29d79fb6024dd75916cdf", "05905136bd908ba4b31a4658cff1182386a1dcb600c29d79fb6024dd75916cdf"},
                    "this$0", "net/torocraft/toroquest/entities/EntityMonolithEye", TORO_GHOST),
        };
    }
}
