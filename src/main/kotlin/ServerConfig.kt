import net.minecraftforge.common.ForgeConfigSpec

class ServerConfig(builder: ForgeConfigSpec.Builder) {
//    private lateinit var x: ForgeConfigSpec.BooleanValue

    init {
        builder.push("general")
        //TODO: add list of dimension IDs to log
        builder.pop()
    }
}