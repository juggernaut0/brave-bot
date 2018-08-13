package bravebot.commands

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser

class MuricaCommand : BaseCommand("\uD83C\uDDFA\uD83C\uDDF8") {
    override fun execute(args: String, author: IUser, channel: IChannel): String {
        return "'MURICA!"
    }
}