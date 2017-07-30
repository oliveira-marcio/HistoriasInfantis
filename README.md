# Histórias Infantis
Projeto final (Capstone) para o Android Developer Nanodegree da Udacity.

O aplicativo consulta o blog Histórias Infantis Abobrinha  (https://historiasinfantisabobrinha.wordpress.com/) e exibe as histórias no aplicativo com layout otimizado para celulares e tablets além de oferecer recursos que estendem a funcionalidade do blog para os dispositivos mobile. 

Principais recursos desenvolvidos no projeto:
- Os dados do blog são obtidos da Wordpress REST API via JSON.
- Opção de salvar as histórias preferidas como favoritas que são visualizadas numa listagem própria
- Uso de ContentProviders, Services e JobScheduler para sincronização periódica dos dados do blog com a base SQLite local, o que permite leitura offline.
- Emissão de notificações quando novas histórias são adicionadas no banco de dados
- 2 tipos de App Widgets implementados e com opção de alteração da configuração dos mesmos.
- Atalho do aplicativo no launcher com App Shortcuts implementados
- Recebimento de notificações via Firebase Cloud Messaging que abrem o aplicativo ou abrem o navegador numa URL enviada como Extra pela notificação
- Aplicativo distribuído em 2 build variants e 2 flavors:
   - *debug*: versão de testes com um menu para remover algumas histórias do banco de dados e testar a sincronização
   - *release*: versão para distribuição em Produção
   - *free*: versão com adMob Native Express Banners e Interstitial Ad's
   - *paid*: versão sem ad's
- Material Design e interface:
   - layout otimizado para celulares e tablets e respectivas orientações de tela
   - Implementado conceito de Surfaces
   - Efeito de parallax scroll nas 2 principais Activities
   - Animação de vertical scroll para Up Button e exibição progressiva da Status Bar
   - Animações de entrada e saída da segunda Activity
   - Implementado ViewPager na segunda Activity para troca das histórias com gesto de swipe horizontal
   - Implementado Navigation Drawer (estático na MainACtivity e dinâmico na outra)
   - Implementado swipe to delete na RecyclerView de exibição de histórias favoritas com exibição de ícone e background "por trás" do ítem deslizado
   - Efeito de animação dos itens da RecyclerView na primeira exibição.
- Outros recursos:
   - Tela de configuração para escolha da ordenação das RecyclerViews e opção de exibição das notificações
   - Opção de compartilhar o link do aplicativo na Play Store através dos principais aplicativos de redes sociais (e-mail, facebook, whatsapp, etc)
   - Exibição dos principais links de contatos do cliente (Abobrinha Studios) nas redes sociais que abrem o navegador ou os respectivos aplicativos (se disponíveis) na página do cliente.
- Testes JUnit e Espresso implementados principalmente para testar a obtenção de dados da API e sincronização com o banco de dados.

# Comentários sobre as principais estratégias de implementação no aplicativo

**Sincronização da API com o banco de dados**

É criada uma tarefa agendada que roda a cada 24 horas e realiza os seguintes passos:
1) Recupera os ID's das histórias marcadas como favoritas
2) Deleta todas as histórias da base
3) Recria a base incluindo novamente as histórias
4) Restaura as marcações prévias de favoritos
5) Indica a quantidade de novas histórias (com base na diferença de registros adicionados no passo 3 e registros deletados no passo 2)

**Distribuição de banners como ítens das RecyclerViews**

2 parâmetros foram criados para definir a distribuição:
- *INITIAL_OFFSET* - define a posição do primeiro banner da RecyclerView 
- *INTERVAL* - define o intervalo de posição entre os banners

Com base nesses parâmetros:
- São feitos cálculos para indicar a quantidade total de ítens da RecyclerView (histórias + banners)
- São feitos cálculos para carregar o ViewHolder apropriado (história ou banner) em cada posição da lista.
- São feitos cálculos para obter a posição correta da história no banco de dados, quando clicada.

**Exibição da história**

A API retorna o conteúdo da história como uma string HTML, então é necessário fazer um parsing da mesma para se obter os dados relevantes. Com base na experiência com outros projetos, colocar textos muito grandes (e ainda com imagens) num único TextView (com ScrollView) deixa a interface bastante pesada, então optei por criar um algoritmo que quebra a história em parágrafos (tag `<P>` do HTML) que são salvos em registros independentes no banco de dados para serem carregados numa RecyclerView que é muito mais performática e elimina a preocupação com o tamanho da história. Este mesmo algoritmo de parsing e quebra dos parágrafos também identifica outros elementos-chave nos mesmos (imagens, autor e indicador de fim da história) para classificá-los e carregá-los em ViewHolders específicos na RecyclerView.
