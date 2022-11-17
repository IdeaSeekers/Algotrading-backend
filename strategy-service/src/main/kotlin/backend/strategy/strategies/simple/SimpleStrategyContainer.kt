package backend.strategy.strategies.simple

import backend.common.model.BotInfo.Status
import backend.strategy.Configuration
import backend.strategy.StrategyController
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SimpleStrategyContainer(
    private val strategy: suspend (Configuration) -> Unit
) : StrategyController {
    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher)
    private lateinit var job: Job


    override fun start(configuration: Configuration): Result<Boolean> {
        job = scope.launch {
            strategy(configuration)
        }

        return Result.success(true)
    }

    override fun status(): Result<Status> =
        Result.success(
            when {
                scope.isActive -> Status.RUNNING
                job.isCompleted -> Status.STOPPED
                else -> Status.UNKNOWN
            }
        )

    override fun pause(): Result<Boolean> {
        return Result.success(false) // TODO: unsupported
    }

    override fun delete(): Result<Boolean> {
        return if (scope.isActive) {
            scope.cancel()
            Result.success(true)
        } else{
            Result.success(false)
        }
    }

    override fun resume(): Result<Boolean> {
        return Result.success(false) // unsupported
    }
}