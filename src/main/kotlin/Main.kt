package org.example

import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

data class Task(
    val id: Int,
    var title: String,
    var description: String,
    var priority: String,
    var dueDate: String,
    var isCompleted: Boolean,
    var category: String,
    val createdAt: String
)

val priorities = listOf("Низкий", "Средний", "Высокий", "Срочный")
val defaultCategories = listOf("Работа", "Личное", "Учеба", "Здоровье", "Финансы")

val tasks = mutableListOf<Task>()
var nextId = 1
val dateFormat = SimpleDateFormat("dd.MM.yyyy")
val currentDate = dateFormat.format(Date())

fun validateTitle(title: String): Boolean = title.isNotBlank()
fun validatePriority(priority: String): Boolean = priorities.contains(priority)
fun validateCategory(category: String): Boolean = defaultCategories.contains(category) || category.isNotBlank()

fun validateDate(dateString: String): Boolean {
    return try {
        dateFormat.parse(dateString)
        true
    } catch (e: Exception) {
        false
    }
}

fun isOverdue(dueDate: String): Boolean {
    return try {
        val due = dateFormat.parse(dueDate)
        val current = dateFormat.parse(currentDate)
        due.before(current) && !dateFormat.format(due).equals(dateFormat.format(current))
    } catch (e: Exception) {
        false
    }
}

fun createTask(title: String, description: String, priority: String, dueDate: String, category: String): Task {
    val task = Task(
        id = nextId++,
        title = title.trim(),
        description = description.trim(),
        priority = priority,
        dueDate = dueDate,
        isCompleted = false,
        category = category,
        createdAt = currentDate
    )
    tasks.add(task)
    return task
}

fun findTaskById(id: Int): Task? = tasks.find { it.id == id }

fun updateTask(
    task: Task,
    newTitle: String,
    newDescription: String,
    newPriority: String,
    newDueDate: String,
    newCategory: String
): Boolean {
    return if (validateTitle(newTitle) && validatePriority(newPriority) && validateCategory(newCategory) && validateDate(
            newDueDate
        )
    ) {
        task.title = newTitle.trim()
        task.description = newDescription.trim()
        task.priority = newPriority
        task.dueDate = newDueDate
        task.category = newCategory
        true
    } else {
        false
    }
}

fun deleteTask(id: Int): Boolean {
    val task = findTaskById(id)
    return task?.let {
        tasks.remove(it)
        true
    } ?: false
}

fun markTaskCompleted(id: Int): Boolean {
    val task = findTaskById(id)
    task?.let {
        it.isCompleted = true
        return true
    }
    return false
}

fun searchTasks(query: String): List<Task> {
    val lowerQuery = query.lowercase()
    return tasks.filter {
        it.title.lowercase().contains(lowerQuery) || it.description.lowercase().contains(lowerQuery)
    }
}

fun filterTasksByStatus(showCompleted: Boolean?): List<Task> = when (showCompleted) {
    true -> tasks.filter { it.isCompleted }
    false -> tasks.filter { !it.isCompleted }
    null -> tasks
}

fun getOverdueTasks(): List<Task> = tasks.filter { !it.isCompleted && isOverdue(it.dueDate) }

fun getTaskStatistics(): Map<String, Any> {
    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val active = total - completed
    val completionRate = if (total > 0) (completed.toDouble() / total * 100) else 0.0

    return mapOf(
        "total" to total,
        "completed" to completed,
        "active" to active,
        "completionRate" to completionRate,
        "priorityDistribution" to tasks.groupingBy { it.priority }.eachCount(),
        "categoryDistribution" to tasks.groupingBy { it.category }.eachCount(),
        "overdueCount" to getOverdueTasks().size
    )
}

fun formatTaskForList(task: Task): String {
    val status = if (task.isCompleted) "[ВЫПОЛНЕНО]" else "[АКТИВНА]"
    val overdue = if (!task.isCompleted && isOverdue(task.dueDate)) " [ПРОСРОЧЕНО]" else ""

    return """
        ID: ${task.id} $status$overdue
        Название: ${task.title}
        Описание: ${if (task.description.isBlank()) "Нет описания" else task.description}
        Категория: ${task.category} | Срок: ${task.dueDate} | Приоритет: ${task.priority}
        Создана: ${task.createdAt}
        ${"-".repeat(50)}
    """.trimIndent()
}

fun displayTasks(taskList: List<Task>) {
    if (taskList.isEmpty()) {
        println("Задачи не найдены")
        return
    }
    taskList.forEach { println(formatTaskForList(it)) }
}

fun displayStatistics() {
    val stats = getTaskStatistics()

    println(
        """
        СТАТИСТИКА СИСТЕМЫ
        ==================
        Всего задач: ${stats["total"]}
        Выполнено: ${stats["completed"]}
        Активных: ${stats["active"]}
        Процент выполнения: ${"%.1f".format(stats["completionRate"])}%
        Просрочено: ${stats["overdueCount"]}
        
        Распределение по приоритетам:
    """.trimIndent()
    )

    (stats["priorityDistribution"] as Map<*, *>).forEach { (priority, count) ->
        println("   $priority: $count")
    }

    println("\nРаспределение по категориям:")
    (stats["categoryDistribution"] as Map<*, *>).forEach { (category, count) ->
        println("   $category: $count")
    }
}

fun readNonEmptyString(prompt: String): String {
    while (true) {
        print(prompt)
        val input = readLine()?.trim()
        if (!input.isNullOrEmpty()) return input
        println("Ошибка: поле не может быть пустым")
    }
}

fun readOptionalString(prompt: String): String {
    print(prompt)
    return readLine()?.trim() ?: ""
}

fun selectFromList(options: List<String>, prompt: String): String {
    println(prompt)
    options.forEachIndexed { index, option -> println("${index + 1}. $option") }

    while (true) {
        print("Выберите вариант (1-${options.size}): ")
        val input = readLine()?.toIntOrNull()
        if (input != null && input in 1..options.size) return options[input - 1]
        println("Неверный выбор")
    }
}

fun readDate(prompt: String): String {
    while (true) {
        print(prompt)
        val input = readLine()?.trim()
        if (!input.isNullOrEmpty() && validateDate(input)) return input
        println("Неверный формат даты. Используйте ДД.ММ.ГГГГ")
    }
}

fun showMainMenu() {
    println(
        """
        
        TASK MASTER - Система управления задачами
        ========================================
        1. Просмотреть задачи
        2. Добавить задачу
        3. Редактировать задачу
        4. Отметить задачу выполненной
        5. Удалить задачу
        6. Поиск задач
        7. Статистика
        8. Выход
        
    """.trimIndent()
    )
}

fun handleViewTasks() {
    println("\nРежим просмотра:")
    println("1. Все задачи")
    println("2. Только активные")
    println("3. Только выполненные")
    println("4. Просроченные")

    when (readLine()?.toIntOrNull()) {
        1 -> displayTasks(tasks)
        2 -> displayTasks(filterTasksByStatus(false))
        3 -> displayTasks(filterTasksByStatus(true))
        4 -> displayTasks(getOverdueTasks())
        else -> println("Неверный выбор")
    }
}

fun handleAddTask() {
    println("\nДобавление новой задачи")

    val title = readNonEmptyString("Название задачи: ")
    val description = readOptionalString("Описание: ")
    val priority = selectFromList(priorities, "Выберите приоритет:")
    val dueDate = readDate("Дата выполнения (ДД.ММ.ГГГГ): ")

    println("Выберите категорию:")
    val categories = defaultCategories + "Другая"
    val selectedCategory = selectFromList(categories, "")

    val category = if (selectedCategory == "Другая") {
        readNonEmptyString("Введите название категории: ")
    } else {
        selectedCategory
    }

    val task = createTask(title, description, priority, dueDate, category)
    println("\nЗадача создана!")
    println(formatTaskForList(task))
}

fun handleEditTask() {
    println("\nРедактирование задачи")
    print("Введите ID задачи: ")
    val taskId = readLine()?.toIntOrNull() ?: run {
        println("Неверный ID")
        return
    }

    val task = findTaskById(taskId) ?: run {
        println("Задача не найдена")
        return
    }

    if (task.isCompleted) {
        println("Нельзя редактировать выполненную задачу")
        return
    }

    println("Текущие данные:")
    println(formatTaskForList(task))
    println("\nВведите новые данные:")

    val newTitle = readOptionalString("Название [${task.title}]: ").ifBlank { task.title }
    val newDescription = readOptionalString("Описание [${task.description}]: ").ifBlank { task.description }
    val newPriority = selectFromList(priorities, "Приоритет [${task.priority}]:")
    val newDueDate = readDate("Дата [${task.dueDate}]: ")

    println("Категория:")
    val categories = defaultCategories + "Другая"
    val selectedCategory = selectFromList(categories, "")

    val newCategory = if (selectedCategory == "Другая") {
        readNonEmptyString("Новая категория: ")
    } else {
        selectedCategory
    }

    if (updateTask(task, newTitle, newDescription, newPriority, newDueDate, newCategory)) {
        println("Задача обновлена!")
    } else {
        println("Ошибка при обновлении")
    }
}

fun handleCompleteTask() {
    println("\nОтметка задачи выполненной")
    print("Введите ID задачи: ")
    val taskId = readLine()?.toIntOrNull() ?: run {
        println("Неверный ID")
        return
    }

    if (markTaskCompleted(taskId)) {
        println("Задача отмечена как выполненная!")
    } else {
        println("Задача не найдена")
    }
}

fun handleDeleteTask() {
    println("\nУдаление задачи")
    print("Введите ID задачи: ")
    val taskId = readLine()?.toIntOrNull() ?: run {
        println("Неверный ID")
        return
    }

    val task = findTaskById(taskId) ?: run {
        println("Задача не найдена")
        return
    }

    println("Удалить задачу:")
    println(formatTaskForList(task))
    print("Подтвердите удаление (введите 'да'): ")

    if (readLine()?.trim()?.lowercase() == "да") {
        if (deleteTask(taskId)) {
            println("Задача удалена!")
        } else {
            println("Ошибка при удалении")
        }
    } else {
        println("Удаление отменено")
    }
}

fun handleSearchTasks() {
    println("\nПоиск задач")
    print("Введите запрос: ")
    val query = readLine()?.trim() ?: ""

    if (query.isBlank()) {
        println("Запрос не может быть пустым")
        return
    }

    val results = searchTasks(query)
    if (results.isEmpty()) {
        println("Задачи не найдены")
    } else {
        println("Найдено задач: ${results.size}")
        displayTasks(results)
    }
}

fun main() {
    println("Добро пожаловать в TaskMaster!")

    createTask("Изучить Kotlin", "Освоить функциональное программирование", "Высокий", "11.10.2025", "Учеба")
    createTask("Купить продукты", "Молоко, хлеб, фрукты", "Средний", "12.10.2025", "Личное")

    while (true) {
        showMainMenu()
        print("Выберите действие: ")

        when (readLine()?.toIntOrNull()) {
            1 -> handleViewTasks()
            2 -> handleAddTask()
            3 -> handleEditTask()
            4 -> handleCompleteTask()
            5 -> handleDeleteTask()
            6 -> handleSearchTasks()
            7 -> displayStatistics()
            8 -> {
                println("До свидания!")
                exitProcess(0)
            }

            else -> println("Неверный выбор")
        }

        println("\nНажмите Enter для продолжения...")
        readLine()
    }
}