package resources

class NotEnoughResourceException(private val type : ResourceType) : Exception() {
    override val message: String
        get() = when(type) {
            ResourceType.CODE -> "Not enough lines of code."
            ResourceType.CASH -> "Not enough cash."
            ResourceType.CAFFEINE -> "Not enough caffeine."
            ResourceType.CASH_OR_SPACE -> "Not enough cash or space in team."
        }
}