package bravebot.commands

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser

class CompileCommand : BaseCommand("compile") {
    override fun execute(args: String, author: IUser, channel: IChannel): String {
        return "This command is not yet available."
    }
}