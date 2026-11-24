package team.segroup11.review.model

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.net.URI
import java.util.Locale

enum class OutputMode { JSON_OBJECT, STRICT_SCHEMA }

data class ModelOptions(
    var baseUrl: String = "",
    var modelId: String = "",
    var mode: OutputMode = OutputMode.JSON_OBJECT,
    var contextTokens: Int = 32768,
    var outputTokens: Int = 4096
)

/** Intentionally no generated toString containing the credential. */
internal class ModelProfile(val options: ModelOptions, val apiKey: String)

internal fun endpoint(value: String): String {
    val uri = try { URI(value.trim()) } catch (_: Exception) { null }
    val host = uri?.host?.lowercase(Locale.ROOT)
    val scheme = uri?.scheme?.lowercase(Locale.ROOT)
    val loopback = host in setOf("localhost", "127.0.0.1", "[::1]")
    if (uri == null || host == null || uri.userInfo != null || uri.rawQuery != null ||
        uri.rawFragment != null || !(scheme == "https" || scheme == "http" && loopback)) {
        throw ModelConfigurationException("请输入 HTTPS 服务地址。本机测试可使用 http://127.0.0.1。")
    }
    if (uri.port > 65535 || uri.port == 0) throw ModelConfigurationException("服务地址端口无效。")
    val port = when {
        uri.port == -1 || scheme == "https" && uri.port == 443 || scheme == "http" && uri.port == 80 -> ""
        else -> ":${uri.port}"
    }
    return "${scheme}://${host}${port}${uri.normalize().rawPath.orEmpty().trimEnd('/')}"
}

internal fun validateBudget(options: ModelOptions) {
    if (options.outputTokens < 1 || options.contextTokens <= options.outputTokens) {
        throw ModelConfigurationException("上下文窗口必须大于预留输出，二者均须为正整数。")
    }
}

@Service(Service.Level.APP)
@State(name = "Group11IssueReviewModel", storages = [Storage("group11IssueReviewModel.xml", roamingType = RoamingType.DISABLED)])
class ModelSettings : PersistentStateComponent<ModelOptions> {
    @Volatile private var saved = ModelOptions()
    override fun getState() = saved.copy()
    override fun loadState(state: ModelOptions) { saved = state.copy() }

    private fun credentialKey(url: String) = CredentialAttributes("CS5351 Group 11 Issue Review | " + url)

    @Synchronized
    internal fun profile(): ModelProfile {
        val options = saved.copy()
        if (options.baseUrl.isBlank()) throw ModelConfigurationException("请在设置中填写模型服务地址。")
        val url = endpoint(options.baseUrl)
        if (options.modelId.isBlank()) throw ModelConfigurationException("请在设置中填写模型 ID。")
        validateBudget(options)
        val secret = PasswordSafe.instance.getPassword(credentialKey(url))?.takeIf { it.isNotBlank() }
            ?: throw ModelConfigurationException("请在设置中为此服务地址保存 API 密钥。")
        return ModelProfile(options.copy(baseUrl = url), secret)
    }

    @Synchronized
    internal fun save(options: ModelOptions, newKey: CharArray, clear: Boolean) {
        validateBudget(options)
        val url = options.baseUrl.takeIf { it.isNotBlank() }?.let(::endpoint).orEmpty()
        if (url.isEmpty() && (newKey.isNotEmpty() || clear)) {
            throw ModelConfigurationException("请先填写密钥对应的服务地址。")
        }
        if (clear || newKey.isNotEmpty()) {
            val credentials = if (clear) null else Credentials("API key", newKey)
            PasswordSafe.instance.set(credentialKey(url), credentials)
        }
        saved = options.copy(baseUrl = url, modelId = options.modelId.trim())
    }
}
