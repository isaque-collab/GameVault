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

export type PlataformaJogoDetalhes = {
    id: number
    nome: string
    slug: string
    requisitoMinimo: string | null
    requisitoRecomendade: string | null
}

export type JogoDetalhes = {
    rawgGameId: number
    nome: string
    descricao: string | null
    dataLancamento: string | null
    imagemFundo: string | null
    notaRawg: number | null
    quantidadeAvaliacoesRawg: number | null
    metacritic: number | null

    mediaAvaliacoesGameVault: number | null
    quantidadeAvaliacoesGameVault: number
    minhaAvaliacao: number | null

    favoritado: boolean | null
    naListaDesejos: boolean | null

    tempoMedioJogo: number | null
    classificacaoEtaria: string | null

    generos: string[]
    plataformas: PlataformaJogoDetalhes[]
    desenvolvedoras: string[]
    publicadoras: string[]
    screenshots: string[]
}