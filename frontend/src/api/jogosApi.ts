import type {BuscaJogosResposta} from '../types/jogo.ts';
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