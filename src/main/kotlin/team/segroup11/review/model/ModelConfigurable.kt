package team.segroup11.review.model

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.ui.DocumentAdapter
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.event.DocumentEvent

class ModelConfigurable : Configurable {
    private val settings get() = service<ModelSettings>()
    private val url = JTextField(40)
    private val model = JTextField()
    private val key = JPasswordField()
    private val clear = JCheckBox("删除此服务地址已保存的密钥")
    private val mode = JComboBox(arrayOf("JSON 对象", "严格 JSON Schema"))
    private val window = JTextField()
    private val output = JTextField()

    override fun getDisplayName() = "GitHub Issue 质量评审"

    init {
        url.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                key.text = ""
                clear.isSelected = false
                key.isEnabled = true
            }
        })
        clear.addActionListener {
            if (clear.isSelected) key.text = ""
            key.isEnabled = !clear.isSelected
        }
    }

    override fun createComponent(): JComponent {
        val form = JPanel(GridBagLayout())
        fun addRow(row: Int, label: String, input: JComponent) {
            val c = GridBagConstraints().apply {
                gridy = row
                anchor = GridBagConstraints.LINE_START
                insets = Insets(4, 4, 4, 8)
            }
            form.add(JLabel(label).apply { labelFor = input }, c)
            c.gridx = 1
            c.weightx = 1.0
            c.fill = GridBagConstraints.HORIZONTAL
            form.add(input, c)
        }
        addRow(0, "服务地址（Base URL）", url)
        addRow(1, "模型 ID", model)
        addRow(2, "API 密钥", key)
        addRow(3, "", JLabel("留空可继续使用此地址在 IDE 凭据库中保存的密钥。"))
        addRow(4, "", clear)
        addRow(5, "服务支持的输出模式", mode)
        addRow(6, "模型上下文上限（token）", window)
        addRow(7, "预留输出（token）", output)
        return JPanel(BorderLayout()).apply { add(form, BorderLayout.NORTH) }
    }

    override fun isModified(): Boolean {
        val state = settings.state
        return url.text != state.baseUrl || model.text != state.modelId || key.document.length != 0 ||
            clear.isSelected || mode.selectedIndex != state.mode.ordinal ||
            window.text != state.contextTokens.toString() || output.text != state.outputTokens.toString()
    }

    override fun reset() {
        val state = settings.state
        url.text = state.baseUrl
        model.text = state.modelId
        mode.selectedIndex = state.mode.ordinal
        window.text = state.contextTokens.toString()
        output.text = state.outputTokens.toString()
        clear.isSelected = false
        key.isEnabled = true
        key.text = ""
    }

    override fun apply() {
        val contextTokens = window.text.trim().toIntOrNull() ?: throw ConfigurationException("请输入有效的上下文上限。")
        val outputTokens = output.text.trim().toIntOrNull() ?: throw ConfigurationException("请输入有效的预留输出。")
        val options = ModelOptions(url.text, model.text, OutputMode.values()[mode.selectedIndex], contextTokens, outputTokens)
        val secret = key.password
        val clearKey = clear.isSelected
        try {
            validateBudget(options)
            if (options.baseUrl.isNotBlank()) endpoint(options.baseUrl)
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                Runnable { settings.save(options, secret, clearKey) }, "保存模型设置", false, null
            )
            reset()
        } catch (e: ModelConfigurationException) {
            throw ConfigurationException(e.message ?: "模型设置无效。")
        } finally {
            secret.fill('\u0000')
        }
    }
}
