package backend.common.model

/**
 * Hyperparameter description.
 * The parameter should be in [min, max] if they are specified.
 */
data class HyperParameterInfo(
    val name: String,
    val description: String,
    val type: Type,
    val min: Double? = null,
    val max: Double? = null,
) {
    enum class Type {
        INT,
        FLOAT,
    }
}