package dev.intentflow.generator

import dev.intentflow.ai.FlowManifest
import dev.intentflow.ai.FlowManifestValidator
import dev.intentflow.ai.FlowManifestYamlParser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.system.exitProcess

public object IntentFlowGenerate {
    private val usage: String = """
        Usage:
          intentflow feature <Name> [--mode core|ai] [--ui compose|viewmodel|none] [--output path]
          intentflow generate feature <Name> [--mode core|ai] [--ui compose|viewmodel|none] [--output path]
          intentflow validate <path-to-manifest.intentflow.yaml>
          intentflow ai-context <path-to-manifest.intentflow.yaml> [--tool codex|claude|gemini|copilot|cursor|generic]
    """.trimIndent()

    @JvmStatic
    public fun main(args: Array<String>) {
        try {
            run(args.toList())
        } catch (error: IllegalArgumentException) {
            System.err.println(error.message ?: usage)
            exitProcess(2)
        }
    }

    public fun run(arguments: List<String>) {
        when (arguments.firstOrNull()) {
            "feature" -> runFeature(arguments)
            "generate" -> {
                require(arguments.getOrNull(1) == "feature") { usage }
                runFeature(arguments.drop(1))
            }
            "validate" -> runValidate(arguments.drop(1))
            "ai-context" -> runAiContext(arguments.drop(1))
            "--help", "-h", "help" -> println(usage)
            else -> throw IllegalArgumentException(usage)
        }
    }

    private fun runFeature(arguments: List<String>) {
        val name = arguments.getOrNull(1)?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException(usage)
        var mode = FlowManifest.Mode.Core
        var ui = UiKind.None
        var output = Path.of(".").toAbsolutePath().normalize()

        var index = 2
        while (index < arguments.size) {
            when (val argument = arguments[index]) {
                "--mode" -> {
                    val value = arguments.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --mode")
                    mode = FlowManifest.Mode.parse(value)
                    index += 2
                }
                "--ui" -> {
                    val value = arguments.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --ui")
                    ui = UiKind.parse(value)
                        ?: throw IllegalArgumentException("Invalid ui '$value'. Expected compose, viewmodel, or none.")
                    index += 2
                }
                "--output" -> {
                    val value = arguments.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --output")
                    output = Path.of(value).toAbsolutePath().normalize()
                    index += 2
                }
                else -> throw IllegalArgumentException("Unknown argument: $argument")
            }
        }

        generate(name = name, mode = mode, ui = ui, output = output)
    }

    private fun runValidate(arguments: List<String>) {
        val path = arguments.firstOrNull()?.let { Path.of(it) }
            ?: throw IllegalArgumentException(usage)
        val manifest = loadManifest(path)
        val result = FlowManifestValidator.validate(manifest)
        if (result.isValid) {
            println("IntentFlow manifest is valid: ${manifest.feature}")
            return
        }

        result.errors.forEach { println("[error] $it") }
        exitProcess(1)
    }

    private fun runAiContext(arguments: List<String>) {
        val path = arguments.firstOrNull()?.let { Path.of(it) }
            ?: throw IllegalArgumentException(usage)
        var tool = AiTool.Generic
        var index = 1
        while (index < arguments.size) {
            when (val argument = arguments[index]) {
                "--tool" -> {
                    val value = arguments.getOrNull(index + 1)
                        ?: throw IllegalArgumentException("Missing value for --tool")
                    tool = AiTool.parse(value)
                        ?: throw IllegalArgumentException("Invalid tool '$value'.")
                    index += 2
                }
                else -> throw IllegalArgumentException("Unknown argument: $argument")
            }
        }

        println(AiContextTemplate.render(loadManifest(path), tool))
    }

    private fun generate(
        name: String,
        mode: FlowManifest.Mode,
        ui: UiKind,
        output: Path
    ) {
        val template = FeatureTemplate(name = name, mode = mode, ui = ui)
        val featureDirectory = output.resolve(name)
        featureDirectory.createDirectories()

        write(featureDirectory.resolve("${name}Contract.kt"), template.contract)
        write(featureDirectory.resolve("${name}Flow.kt"), template.flow)
        write(featureDirectory.resolve("${name}Effects.kt"), template.effects)
        write(featureDirectory.resolve("${name}Projection.kt"), template.projection)
        template.adapter?.let { write(featureDirectory.resolve("${name}Adapter.kt"), it) }
        write(featureDirectory.resolve("${name}FlowTest.kt"), template.tests)

        if (mode == FlowManifest.Mode.Ai) {
            val manifest = template.manifest
            write(featureDirectory.resolve("$name.intentflow.yaml"), manifest)
            val parsed = FlowManifestYamlParser.parse(manifest)
            write(featureDirectory.resolve("$name.ai-context.md"), AiContextTemplate.render(parsed, AiTool.Generic))
        }

        println("Generated ${mode.name.lowercase()} feature $name at $featureDirectory")
    }

    private fun loadManifest(path: Path): FlowManifest {
        require(Files.exists(path)) { "Manifest does not exist: $path" }
        return FlowManifestYamlParser.parse(Files.readString(path))
    }

    private fun write(path: Path, content: String) {
        path.writeText(content.trimEnd() + "\n")
    }
}
