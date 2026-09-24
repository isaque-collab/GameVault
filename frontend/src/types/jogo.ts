export type JogoResumo = {
    rawgGameId: number
    nome: string
    dataLancamento: string | null
    imagemFundo: string | null
    notaRawg: number | null
    quantidadeAvaliacoesRawg: number | null
    metacritic: number | null
}

export type BuscaJogosResposta = {
    pagina: number
    totalResultados: number
    jogos: JogoResumo[]
}