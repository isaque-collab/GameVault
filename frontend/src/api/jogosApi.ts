import type {
    BuscaJogosResposta,
    FiltrosCatalogoJogos,
    JogoDetalhes,
} from '../types/jogo.ts';
import apiFetch from './http.ts';

export function buscarJogos(
    filtros: FiltrosCatalogoJogos = {},
): Promise<BuscaJogosResposta> {
    const parametros = new URLSearchParams()

    if (filtros.nome?.trim()) {
        parametros.set('nome', filtros.nome.trim())
    }

    if (filtros.genero?.trim()) {
        parametros.set('genero', filtros.genero.trim())
    }

    if (filtros.ordenacao){
        parametros.set('ordenacao', filtros.ordenacao)
    }

    parametros.set(
        'pagina',
        String(filtros.pagina ?? 1),
    )

    return apiFetch<BuscaJogosResposta>(
        `/jogos?${parametros.toString()}`,
    )
}

export function buscarJogosPopulares(
    pagina = 1,
): Promise<BuscaJogosResposta> {
    return apiFetch<BuscaJogosResposta>(
        `/jogos/populares?pagina=${pagina}`,
    )
}

export function buscarLancamentosRecentes(
    pagina = 1,
): Promise<BuscaJogosResposta> {
    return apiFetch<BuscaJogosResposta>(
        `/jogos/lancamentos-recentes?pagina=${pagina}`,
    )
}

export function buscarJogosMaisBemAvaliados(
    pagina = 1,
): Promise<BuscaJogosResposta> {
    return apiFetch<BuscaJogosResposta>(
        `/jogos/mais-bem-avaliados?pagina=${pagina}`,
    )
}

export function buscarDetalhesJogo(
    rawgGameId: number,
): Promise<JogoDetalhes> {
    return apiFetch<JogoDetalhes>(
        `/jogos/${rawgGameId}`,
    )
}