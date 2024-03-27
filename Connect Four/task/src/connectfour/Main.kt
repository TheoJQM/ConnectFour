package connectfour

val questionDimension = """
            Set the board dimensions (Rows x Columns)
            Press Enter for default (6 x 7)
            """.trimIndent()

val gameInfo = """
    %s VS %s
    %d X %d board
""".trimIndent()

val questionNumberGames = """
    Do you want to play single or multiple games?
    For a single game, input 1 or press Enter
    Input a number of games:
    """.trimIndent()

class ConnectFour {
    private var player1 = Pair("", "")
    private var player2 = Pair("", "")
    private var currentPlayer = Pair("", "")
    private var score = Pair(0, 0)

    private var nbRows = 6
    private var nbColumns = 7
    private lateinit var board: MutableList<MutableList<String>>

    private val dimensionRegex = Regex("""\s*\d+\s*[xX]\s*\d+\s*""")
    private var isGameFinished = false
    private var endCall = true

    private var multiGames = Pair(false, 1)
    private var gamePlayed = 1

    fun play() {
        askPlayerNames()
        askBoardDimension()
        askMultiGames()
        println(gameInfo.format(player1.first, player2.first, nbRows, nbColumns))
        if (!multiGames.first) println("Single game") else println("Total ${multiGames.second} games")

        while (gamePlayed <= multiGames.second && endCall) {
            isGameFinished = false
            createBoard()
            setCurrentPlayer()
            if (multiGames.first) println("Game #$gamePlayed")
            showBoard()
            while (!isGameFinished){
                println("${currentPlayer.first}'s turn:")
                val column = readln()
                if (column == "end") {
                    isGameFinished = true
                    endCall = false
                } else checkPlayerChoice(column)
            }
            if (multiGames.first && endCall) println("Score\n${player1.first}: ${score.first} ${player2.first}: ${score.second}")
        }
        println("Game over!")
    }

    private fun askPlayerNames() {
        println("Connect Four")
        println("First player's name:")
        player1 = player1.copy(readln(), "o")
        println("Second player's name:")
        player2 = player2.copy(readln(), "*")
    }

    private fun setCurrentPlayer() {
        currentPlayer = if (gamePlayed % 2 == 1) player1 else player2
    }

    private fun askBoardDimension() {
        println(questionDimension)
        var dimension = readln()

        while (!checkDimension(dimension)) {
            println(questionDimension)
            dimension = readln()
        }
    }

    private fun checkDimension(dimension: String): Boolean {
        return when {
            dimension == "" -> true
            !dimensionRegex.matches(dimension) -> {
                println("Invalid input")
                false
            }

            dimension.split(Regex("[xX]")).first().trim().toInt() !in 5..9 -> {
                println("Board rows should be from 5 to 9")
                false
            }

            dimension.split(Regex("[xX]")).last().toString().trim().toInt() !in 5..9 -> {
                println("Board columns should be from 5 to 9")
                false
            }

            else -> {
                nbRows = dimension.split(Regex("[xX]")).first().toString().trim().toInt()
                nbColumns = dimension.split(Regex("[xX]")).last().toString().trim().toInt()
                true
            }
        }
    }

    private fun createBoard() {
        board = MutableList(nbRows) {
            MutableList(nbColumns) {" "}
        }
    }

    private fun askMultiGames() {
        println(questionNumberGames)
        var nbGames = readln()
        val multiGameRegex = Regex("""([1-9][0-9]*)?""")
        while (!multiGameRegex.matches(nbGames)) {
            println("Invalid input")
            println(questionNumberGames)
            nbGames = readln()
        }
        if (nbGames != "" && nbGames != "1") multiGames = multiGames.copy(true, nbGames.toInt())
    }

    private fun checkPlayerChoice(choice: String) {
        when {
            !Regex("""\d+""").matches(choice) -> println("Incorrect column number")
            choice.toInt() !in 1..nbColumns -> println("The column number is out of range (1 - $nbColumns)")
            isColumnFull(choice.toInt()) -> println("Column $choice is full")
            else -> putDiscs(choice.toInt() - 1)
        }
    }

    private fun isColumnFull(column: Int) = board[0][column - 1] != " "

    private fun putDiscs(column: Int) {
        for (i in nbRows - 1 downTo  0 ) {
            if (board[i][column] == " ") {
                board[i][column] = currentPlayer.second
                if (!checkState(i, column)) {
                    changePlayer()
                    showBoard()
                }
                break
            }
        }
    }

    private fun changePlayer() {
        currentPlayer = if (currentPlayer == player1)  player2 else player1
    }

    private fun checkState(x: Int, y: Int): Boolean {
        val haveWinner = checkHorizontalWin(x, y) || checkVerticalWin(x, y) || checkLeftDiagonalWin(x, y) || checkRightDiagonalWin(x, y)
        val draw = isBoardFull()

        return when {
            haveWinner -> {
                showBoard()
                println("Player ${currentPlayer.first} won")
                score = if (currentPlayer == player1) score.copy(first = score.first + 2) else score.copy(second = score.second + 2)
                isGameFinished = true
                gamePlayed++
                true
            }
            draw -> {
                showBoard()
                println("It is a draw")
                score = score.copy(score.first + 1, score.second + 1)
                isGameFinished = true
                gamePlayed++
                true
            }
            else -> false
        }
    }

    private fun isBoardFull(): Boolean {
        return board[0].all { it != " " }
    }

    private fun checkHorizontalWin(x: Int, y: Int): Boolean {
        var count = 1
        for (i in y - 1 downTo 0) if (board[x][i] == currentPlayer.second)  count++ else break
        for (i in y + 1 until  nbColumns) if (board[x][i] == currentPlayer.second)  count++ else break
        return count >= 4
    }

    private fun checkVerticalWin(x: Int, y: Int): Boolean {
        var count = 1
        for (i in x - 1 downTo 0) if (board[i][y] == currentPlayer.second)  count++ else break
        for (i in x + 1 until  nbRows) if (board[i][y] == currentPlayer.second)  count++ else break
        return count == 4
    }

    private fun checkLeftDiagonalWin(x: Int, y: Int): Boolean {
        var count = 1
        var (tempX, tempY) = x - 1 to y - 1
        while (tempX in 0..x && tempY in 0..y) if (board[tempX][tempY] == currentPlayer.second) {count++; tempX--; tempY--} else break
        tempX = x + 1
        tempY = y + 1
        while (tempX in x until nbRows && tempY in y until nbColumns) if (board[tempX][tempY] == currentPlayer.second) {count++; tempX++; tempY++} else break
        return count == 4
    }

    private fun checkRightDiagonalWin(x: Int, y: Int): Boolean {
        var count = 1
        var (tempX, tempY) = x + 1 to y - 1
        while (tempX in x until nbRows && tempY in 0..y) if (board[tempX][tempY] == currentPlayer.second) {count++; tempX++; tempY--} else break
        tempX = x - 1
        tempY = y + 1
        while (tempX in 0 until x && tempY in y until nbColumns) if (board[tempX][tempY] == currentPlayer.second) {count++; tempX--; tempY++} else break
        return count == 4
    }

    private fun showBoard() {
        println(" " + (1..nbColumns).joinToString(" "))
        repeat(nbRows) { x ->
            println("║${(0 until nbColumns).joinToString("║") { y -> board[x][y] }}║")
        }
        println("╚${"═╩".repeat(nbColumns - 1)}═╝")
    }
}

fun main() {
    val game = ConnectFour()
    game.play()
}