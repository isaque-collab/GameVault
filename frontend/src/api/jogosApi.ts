import type {
    BuscaJogosResposta,
    JogoDetalhes,
} from '../types/jogo.ts';
import apiFetch from './http.ts';

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