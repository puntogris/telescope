package com.puntogris.telescope.domain

import com.intellij.openapi.application.PluginPathManager
import java.sql.Connection
import java.sql.DriverManager
import java.nio.file.Paths
import java.nio.file.Files
import kotlin.io.path.absolutePathString

object Database {

    init {
        Class.forName("org.sqlite.JDBC")
    }

    private lateinit var connectionUrl: String

    private fun newConnection(): Connection = DriverManager.getConnection(connectionUrl)

    fun init(projectName: String) {
        val pluginDataPath = Paths.get(PluginPathManager.getPluginHomePath("drawable-finder"))
        val dbName = projectName.replace(" ", "").replace(".", "").plus(".db")
        val dbPath = pluginDataPath.resolve(dbName).absolutePathString()

        Files.createDirectories(pluginDataPath)
        connectionUrl = "jdbc:sqlite:$dbPath"

        val connection = newConnection()
        connection.createStatement().use { stmt ->
            stmt.execute("CREATE TABLE IF NOT EXISTS example (id INTEGER PRIMARY KEY, name TEXT)")
        }
        connection.close()
    }

    fun insertEmbedding() {
        val connection = newConnection()
        connection.createStatement().use { stmt ->
            stmt.execute("INSERT INTO example (name) VALUES ('new data')")
        }
        connection.close()
    }

    fun getEmbeddings(): List<String> {
        val result = mutableListOf<String>()

        val connection = newConnection()
        val selectStmt = connection.createStatement()
        val resultSet = selectStmt.executeQuery("SELECT * FROM example")

        while (resultSet.next()) {
            val name = resultSet.getString("name")
            result.add(name)
        }
        selectStmt.close()
        connection.close()
        return result
    }
}