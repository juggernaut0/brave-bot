package bravebot.commands

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser

class HelloCommand : BaseCommand("hello", "hey", "hi") {
    override val helpText = makeHelpText("Say hello!")

    override fun execute(args: String, author: IUser, channel: IChannel): String {
        return "Hello ${author.name}!"
    }
}
